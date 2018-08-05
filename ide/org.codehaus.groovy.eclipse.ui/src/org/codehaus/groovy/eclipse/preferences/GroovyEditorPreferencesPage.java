/*
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.preferences;

import java.util.Arrays;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.editor.GroovyColorManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class GroovyEditorPreferencesPage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

    public GroovyEditorPreferencesPage() {
        super(GRID);
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        // Category Methods
        ColorFieldEditor gdkMethodEditor = createColorEditor(
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR,
            "GroovyEditorPreferencesPage.GJDK_method_color");

        // Primitive Types
        ColorFieldEditor primitivesEditor = createColorEditor(
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_PRIMITIVES_COLOR,
            "GroovyEditorPreferencesPage.Primitives_color");

        // Other Keywords
        ColorFieldEditor keywordEditor = createColorEditor(
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR,
            "GroovyEditorPreferencesPage.Keywords_color");

        // Assert Keyword
        ColorFieldEditor assertEditor = createColorEditor(
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ASSERT_COLOR,
            "GroovyEditorPreferencesPage.Assert_color");

        // Return Keyword
        ColorFieldEditor returnEditor = createColorEditor(
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR,
            "GroovyEditorPreferencesPage.Return_color");

        // Semantic highlighting
        Label l = new Label(getFieldEditorParent(), SWT.NONE);
        l.setText("\n\n" + Messages.getString("GroovyEditorPreferencesPage.SemanticHighlightingPrefs"));
        Composite c = new Composite(getFieldEditorParent(), SWT.NONE | SWT.BORDER);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        c.setLayoutData(gd);
        c.setLayout(new FillLayout(SWT.VERTICAL));

        addField(new BooleanFieldEditor(
            PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING,
            Messages.getString("GroovyEditorPreferencesPage.SemanticHighlightingToggle"), c));
        addField(new BooleanFieldEditor(
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_SLASHY_STRINGS,
            Messages.getString("GroovyEditorPreferencesPage.DollarSlashyHighlightingToggle"), c));

        PreferenceLinkArea area = new PreferenceLinkArea(c, SWT.WRAP,
            "org.eclipse.jdt.ui.preferences.JavaEditorColoringPreferencePage",
            "\n" + Messages.getString("GroovyEditorPreferencesPage.InheritedJavaColorsDescription"),
            (IWorkbenchPreferenceContainer) getContainer(), null);
        area.getControl().setLayoutData(gd);

        // Copy Java Preferences
        Composite parent = getFieldEditorParent();
        Button javaColorButton = new Button(parent, SWT.BUTTON1);

        javaColorButton.setText(Messages.getString("GroovyEditorPreferencesPage.Copy_Java_Color_Preferences"));
        javaColorButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
            IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();

            Arrays.asList(primitivesEditor, keywordEditor, assertEditor).forEach(
                editor -> editor.getColorSelector().setColorValue(PreferenceConverter.getColor(store, IJavaColorConstants.JAVA_KEYWORD)));

            returnEditor.getColorSelector().setColorValue(PreferenceConverter.getColor(store, IJavaColorConstants.JAVA_KEYWORD_RETURN));
        }));
    }

    private ColorFieldEditor createColorEditor(String preference, String nls) {
        Composite parent = getFieldEditorParent();
        addField(new SpacerFieldEditor(parent));
        ColorFieldEditor colorFieldEditor = new ColorFieldEditor(preference, Messages.getString(nls), parent);
        addField(colorFieldEditor);
        addField(new BooleanFieldEditor(
            preference + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX,
            "  " + Messages.getString("GroovyEditorPreferencesPage.BoldToggle"),
            BooleanFieldEditor.SEPARATE_LABEL, getFieldEditorParent()));
        return colorFieldEditor;
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected String getPageId() {
        return "org.codehaus.groovy.eclipse.preferences.editor";
    }

    @Override
    public boolean performOk() {
        boolean success = super.performOk();
        if (success) {
            GroovyColorManager colorManager = GroovyPlugin.getDefault().getTextTools().getColorManager();
            colorManager.uninitialize();
            colorManager.initialize();
        }
        return success;
    }

    private static class SpacerFieldEditor extends FieldEditor {

        private Label spacer;

        private SpacerFieldEditor(Composite parent) {
            spacer = new Label(parent, SWT.NONE);
            GridData gd = new GridData();
            spacer.setLayoutData(gd);
        }

        @Override
        protected void adjustForNumColumns(int numColumns) {
            ((GridData) spacer.getLayoutData()).horizontalSpan = numColumns;
        }

        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            GridData gd = new GridData();
            gd.horizontalSpan = numColumns;
            spacer.setLayoutData(gd);
        }

        @Override
        protected void doLoad() {
        }

        @Override
        protected void doLoadDefault() {
        }

        @Override
        protected void doStore() {
        }

        @Override
        public int getNumberOfControls() {
            return 0;
        }

        @Override
        public void store() {
        }
    }
}
