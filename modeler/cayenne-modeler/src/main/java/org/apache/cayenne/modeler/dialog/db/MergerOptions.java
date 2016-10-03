/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */

package org.apache.cayenne.modeler.dialog.db;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.configuration.DataChannelDescriptor;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.dba.JdbcAdapter;
import org.apache.cayenne.dbsync.merge.AbstractToDbToken;
import org.apache.cayenne.dbsync.merge.DbMerger;
import org.apache.cayenne.dbsync.merge.MergeDirection;
import org.apache.cayenne.dbsync.merge.MergerContext;
import org.apache.cayenne.dbsync.merge.MergerToken;
import org.apache.cayenne.dbsync.merge.ModelMergeDelegate;
import org.apache.cayenne.dbsync.merge.factory.MergerTokenFactory;
import org.apache.cayenne.dbsync.merge.factory.MergerTokenFactoryProvider;
import org.apache.cayenne.dbsync.naming.DefaultObjectNameGenerator;
import org.apache.cayenne.dbsync.reverse.db.DbLoader;
import org.apache.cayenne.dbsync.reverse.db.DbLoaderConfiguration;
import org.apache.cayenne.dbsync.reverse.db.LoggingDbLoaderDelegate;
import org.apache.cayenne.dbsync.reverse.filters.FiltersConfig;
import org.apache.cayenne.dbsync.reverse.filters.PatternFilter;
import org.apache.cayenne.dbsync.reverse.filters.TableFilter;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.map.event.EntityEvent;
import org.apache.cayenne.map.event.MapEvent;
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.dialog.ValidationResultBrowser;
import org.apache.cayenne.modeler.event.AttributeDisplayEvent;
import org.apache.cayenne.modeler.event.EntityDisplayEvent;
import org.apache.cayenne.modeler.event.RelationshipDisplayEvent;
import org.apache.cayenne.modeler.pref.DBConnectionInfo;
import org.apache.cayenne.modeler.util.CayenneController;
import org.apache.cayenne.project.Project;
import org.apache.cayenne.resource.Resource;
import org.apache.cayenne.swing.BindingBuilder;
import org.apache.cayenne.swing.ObjectBinding;
import org.apache.cayenne.validation.ValidationResult;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class MergerOptions extends CayenneController {

    protected MergerOptionsView view;
    protected ObjectBinding sqlBinding;

    protected DBConnectionInfo connectionInfo;
    protected DataMap dataMap;
    protected JdbcAdapter adapter;
    protected String textForSQL;

    protected MergerTokenSelectorController tokens;
    protected String defaultSchema;
    private MergerTokenFactoryProvider mergerTokenFactoryProvider;

    public MergerOptions(ProjectController parent,
                         String title,
                         DBConnectionInfo connectionInfo,
                         DataMap dataMap,
                         String defaultSchema,
                         MergerTokenFactoryProvider mergerTokenFactoryProvider) {
        super(parent);

        this.mergerTokenFactoryProvider = mergerTokenFactoryProvider;
        this.dataMap = dataMap;
        this.tokens = new MergerTokenSelectorController(parent);
        this.view = new MergerOptionsView(tokens.getView());
        this.connectionInfo = connectionInfo;
        this.defaultSchema = defaultSchema;
        /*
         * TODO:? this.generatorDefaults = (DBGeneratorDefaults) parent
         * .getPreferenceDomainForProject() .getDetail("DbGenerator",
         * DBGeneratorDefaults.class, true);
         */
        this.view.setTitle(title);
        initController();

        // tables.updateTables(dataMap);
        prepareMigrator();
        // generatorDefaults.configureGenerator(generator);
        createSQL();
        refreshView();
    }

    public Component getView() {
        return view;
    }

    /*
     * public DBGeneratorDefaults getGeneratorDefaults() { return generatorDefaults; }
     */

    public String getTextForSQL() {
        return textForSQL;
    }

    protected void initController() {

        BindingBuilder builder = new BindingBuilder(
                getApplication().getBindingFactory(),
                this);

        sqlBinding = builder.bindToTextArea(view.getSql(), "textForSQL");

        builder.bindToAction(view.getGenerateButton(), "generateSchemaAction()");
        builder.bindToAction(view.getSaveSqlButton(), "storeSQLAction()");
        builder.bindToAction(view.getCancelButton(), "closeAction()");

        // refresh SQL if different tables were selected
        view.getTabs().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (view.getTabs().getSelectedIndex() == 1) {
                    // this assumes that some tables where checked/unchecked... not very
                    // efficient
                    refreshGeneratorAction();
                }
            }
        });
    }

    /**
     * check database and create the {@link List} of {@link MergerToken}s
     */
    protected void prepareMigrator() {
        try {
            adapter = (JdbcAdapter) connectionInfo.makeAdapter(getApplication().getClassLoadingService());

            MergerTokenFactory mergerTokenFactory = mergerTokenFactoryProvider.get(adapter);
            tokens.setMergerTokenFactory(mergerTokenFactory);


            FiltersConfig filters = FiltersConfig.create(null, defaultSchema, TableFilter.everything(),
                    PatternFilter.INCLUDE_NOTHING);

            DbMerger merger = DbMerger.builder(mergerTokenFactory)
                    .filters(filters)
                    .build();

            DbLoaderConfiguration config = new DbLoaderConfiguration();
            config.setFiltersConfig(filters);

            DataSource dataSource = connectionInfo.makeDataSource(getApplication().getClassLoadingService());

            DataMap dbImport = new DataMap();
            try (Connection conn = dataSource.getConnection();) {
                new DbLoader(conn,
                        adapter,
                        new LoggingDbLoaderDelegate(LogFactory.getLog(DbLoader.class)),
                        new DefaultObjectNameGenerator())
                        .load(dbImport, config);

            } catch (SQLException e) {
                throw new CayenneRuntimeException("Can't doLoad dataMap from db.", e);
            }

            tokens.setTokens(merger.createMergeTokens(dataMap, dbImport));
        } catch (Exception ex) {
            reportError("Error loading adapter", ex);
        }
    }

    /**
     * Returns SQL statements generated for selected schema generation options.
     */
    protected void createSQL() {
        // convert them to string representation for display
        StringBuilder buf = new StringBuilder();

        Iterator<MergerToken> it = tokens.getSelectedTokens().iterator();
        String batchTerminator = adapter.getBatchTerminator();

        String lineEnd = batchTerminator != null ? "\n" + batchTerminator + "\n\n" : "\n\n";
        while (it.hasNext()) {
            MergerToken token = it.next();

            if (token instanceof AbstractToDbToken) {
                AbstractToDbToken tdb = (AbstractToDbToken) token;
                for (String sql : tdb.createSql(adapter)) {
                    buf.append(sql);
                    buf.append(lineEnd);
                }
            }
        }

        textForSQL = buf.toString();
    }

    protected void refreshView() {

        /*
         * for (int i = 0; i < optionBindings.length; i++) {
         * optionBindings[i].updateView(); }
         */

        sqlBinding.updateView();
    }

    // ===============
    // Actions
    // ===============

    /**
     * Starts options dialog.
     */
    public void startupAction() {
        view.pack();
        view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        view.setModal(true);
        makeCloseableOnEscape();
        centerView();
        view.setVisible(true);
    }

    public void refreshGeneratorAction() {
        // prepareMigrator();
        refreshSQLAction();
    }

    /**
     * Updates a text area showing generated SQL.
     */
    public void refreshSQLAction() {
        // sync generator with defaults, make SQL, then sync the view...
        // generatorDefaults.configureGenerator(generator);
        createSQL();
        sqlBinding.updateView();
    }

    /**
     * Performs configured schema operations via DbGenerator.
     */
    public void generateSchemaAction() {
        refreshGeneratorAction();

        // sanity check...
        List<MergerToken> tokensToMigrate = tokens.getSelectedTokens();
        if (tokensToMigrate.isEmpty()) {
            JOptionPane.showMessageDialog(getView(), "Nothing to migrate.");
            return;
        }

        final ProjectController c = getProjectController();

        final Object src = this;
        final DataChannelDescriptor domain = (DataChannelDescriptor) getProjectController()
                .getProject()
                .getRootNode();
        final DataNodeDescriptor node = getProjectController().getCurrentDataNode();

        ModelMergeDelegate delegate = new ModelMergeDelegate() {

            public void dbAttributeAdded(DbAttribute att) {
                if (c.getCurrentDbEntity() == att.getEntity()) {
                    c.fireDbAttributeDisplayEvent(new AttributeDisplayEvent(src, att, att
                            .getEntity(), dataMap, domain));
                }
            }

            public void dbAttributeModified(DbAttribute att) {
                if (c.getCurrentDbEntity() == att.getEntity()) {
                    c.fireDbAttributeDisplayEvent(new AttributeDisplayEvent(src, att, att
                            .getEntity(), dataMap, domain));
                }
            }

            public void dbAttributeRemoved(DbAttribute att) {
                if (c.getCurrentDbEntity() == att.getEntity()) {
                    c.fireDbAttributeDisplayEvent(new AttributeDisplayEvent(src, att, att
                            .getEntity(), dataMap, domain));
                }
            }

            public void dbEntityAdded(DbEntity ent) {
                c.fireDbEntityEvent(new EntityEvent(src, ent, MapEvent.ADD));
                c.fireDbEntityDisplayEvent(new EntityDisplayEvent(
                        src,
                        ent,
                        dataMap,
                        node,
                        domain));
            }

            public void dbEntityRemoved(DbEntity ent) {
                c.fireDbEntityEvent(new EntityEvent(src, ent, MapEvent.REMOVE));
                c.fireDbEntityDisplayEvent(new EntityDisplayEvent(
                        src,
                        ent,
                        dataMap,
                        node,
                        domain));
            }

            public void dbRelationshipAdded(DbRelationship rel) {
                if (c.getCurrentDbEntity() == rel.getSourceEntity()) {
                    c.fireDbRelationshipDisplayEvent(new RelationshipDisplayEvent(
                            src,
                            rel,
                            rel.getSourceEntity(),
                            dataMap,
                            domain));
                }
            }

            public void dbRelationshipRemoved(DbRelationship rel) {
                if (c.getCurrentDbEntity() == rel.getSourceEntity()) {
                    c.fireDbRelationshipDisplayEvent(new RelationshipDisplayEvent(
                            src,
                            rel,
                            rel.getSourceEntity(),
                            dataMap,
                            domain));
                }
            }

            public void objAttributeAdded(ObjAttribute att) {
                if (c.getCurrentObjEntity() == att.getEntity()) {
                    c.fireObjAttributeDisplayEvent(new AttributeDisplayEvent(
                            src,
                            att,
                            att.getEntity(),
                            dataMap,
                            domain));
                }
            }

            public void objAttributeModified(ObjAttribute att) {
                if (c.getCurrentObjEntity() == att.getEntity()) {
                    c.fireObjAttributeDisplayEvent(new AttributeDisplayEvent(
                            src,
                            att,
                            att.getEntity(),
                            dataMap,
                            domain));
                }
            }

            public void objAttributeRemoved(ObjAttribute att) {
                if (c.getCurrentObjEntity() == att.getEntity()) {
                    c.fireObjAttributeDisplayEvent(new AttributeDisplayEvent(
                            src,
                            att,
                            att.getEntity(),
                            dataMap,
                            domain));
                }
            }

            public void objEntityAdded(ObjEntity ent) {
                c.fireObjEntityEvent(new EntityEvent(src, ent, MapEvent.ADD));
                c.fireObjEntityDisplayEvent(new EntityDisplayEvent(
                        src,
                        ent,
                        dataMap,
                        node,
                        domain));
            }

            public void objEntityRemoved(ObjEntity ent) {
                c.fireObjEntityEvent(new EntityEvent(src, ent, MapEvent.REMOVE));
                c.fireObjEntityDisplayEvent(new EntityDisplayEvent(
                        src,
                        ent,
                        dataMap,
                        node,
                        domain));
            }

            public void objRelationshipAdded(ObjRelationship rel) {
                if (c.getCurrentObjEntity() == rel.getSourceEntity()) {
                    c.fireObjRelationshipDisplayEvent(new RelationshipDisplayEvent(
                            src,
                            rel,
                            rel.getSourceEntity(),
                            dataMap,
                            domain));
                }
            }

            public void objRelationshipRemoved(ObjRelationship rel) {
                if (c.getCurrentObjEntity() == rel.getSourceEntity()) {
                    c.fireObjRelationshipDisplayEvent(new RelationshipDisplayEvent(
                            src,
                            rel,
                            rel.getSourceEntity(),
                            dataMap,
                            domain));
                }
            }

        };

        try {
            DataSource dataSource = connectionInfo.makeDataSource(getApplication()
                    .getClassLoadingService());

            MergerContext mergerContext = MergerContext.builder(dataMap)
                    .syntheticDataNode(dataSource, adapter)
                    .delegate(delegate)
                    .build();

            boolean modelChanged = false;
            for (MergerToken tok : tokensToMigrate) {
                int numOfFailuresBefore = mergerContext
                        .getValidationResult()
                        .getFailures()
                        .size();
                tok.execute(mergerContext);
                if (!modelChanged && tok.getDirection().equals(MergeDirection.TO_MODEL)) {
                    modelChanged = true;
                }

                if (numOfFailuresBefore == mergerContext
                        .getValidationResult()
                        .getFailures()
                        .size()) {
                    // looks like the token executed without failures
                    tokens.removeToken(tok);
                }
            }

            if (modelChanged) {
                // mark the model as unsaved
                Project project = getApplication().getProject();
                project.setModified(true);

                ProjectController projectController = getApplication()
                        .getFrameController()
                        .getProjectController();
                projectController.setDirty(true);
            }

            ValidationResult failures = mergerContext.getValidationResult();

            if (failures == null || !failures.hasFailures()) {
                JOptionPane.showMessageDialog(getView(), "Migration Complete.");
            } else {
                new ValidationResultBrowser(this).startupAction(
                        "Migration Complete",
                        "Migration finished. The following problem(s) were ignored.",
                        failures);
            }
        } catch (Throwable th) {
            reportError("Migration Error", th);
        }
    }

    /**
     * Allows user to save generated SQL in a file.
     */
    public void storeSQLAction() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setDialogTitle("Save SQL Script");

        Resource projectDir = getApplication().getProject().getConfigurationResource();

        if (projectDir != null) {
            fc.setCurrentDirectory(new File(projectDir.getURL().getPath()));
        }

        if (fc.showSaveDialog(getView()) == JFileChooser.APPROVE_OPTION) {
            refreshGeneratorAction();

            try {
                File file = fc.getSelectedFile();
                FileWriter fw = new FileWriter(file);
                PrintWriter pw = new PrintWriter(fw);
                pw.print(textForSQL);
                pw.flush();
                pw.close();
            } catch (IOException ex) {
                reportError("Error Saving SQL", ex);
            }
        }
    }

    private ProjectController getProjectController() {
        return getApplication().getFrameController().getProjectController();
    }

    public void closeAction() {
        view.dispose();
    }
}
