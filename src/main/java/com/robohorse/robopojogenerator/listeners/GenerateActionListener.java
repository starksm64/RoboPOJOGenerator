package com.robohorse.robopojogenerator.listeners;

import com.robohorse.robopojogenerator.errors.RoboPluginException;
import com.robohorse.robopojogenerator.generator.consts.AnnotationItem;
import com.robohorse.robopojogenerator.generator.consts.LanguageItem;
import com.robohorse.robopojogenerator.generator.utils.ClassGenerateHelper;
import com.robohorse.robopojogenerator.injections.Injector;
import com.robohorse.robopojogenerator.models.GenerationModel;
import com.robohorse.robopojogenerator.services.MessageService;
import com.robohorse.robopojogenerator.view.GeneratorVew;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.inject.Inject;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Created by vadim on 24.09.16.
 */
public class GenerateActionListener implements ActionListener {

    @Inject
    MessageService      messageService;
    @Inject
    ClassGenerateHelper classGenerateHelper;

    private GuiFormEventListener eventListener;
    private GeneratorVew         generatorVew;


    public GenerateActionListener(GeneratorVew generatorVew,
                                  GuiFormEventListener eventListener) {

        this.generatorVew = generatorVew;
        this.eventListener = eventListener;
        Injector.getAppComponent().inject(this);
    }


    public void actionPerformed(ActionEvent e) {

        final JTextArea  textArea  = generatorVew.getTextArea();
        final JTextField textField = generatorVew.getClassNameTextField();

        final LanguageItem languageItem = resolveLanguageItem();
        changeAnnotation(languageItem);

        final AnnotationItem annotationItem = resolveAnnotationItem();

        final boolean rewriteClasses = generatorVew.getRewriteExistingClassesCheckBox()
                .isSelected();
        final boolean useSetters = generatorVew.getUseSettersCheckBox().isSelected();
        final boolean useGetters = generatorVew.getUseGettersCheckBox().isSelected();

        final String content   = textArea.getText();
        final String className = textField.getText();
        try {
            classGenerateHelper.validateClassName(className);
            classGenerateHelper.validateJsonContent(content);
            eventListener.onJsonDataObtained(new GenerationModel
                    .Builder()
                    .setLanguageItem(languageItem)
                    .setAnnotationItem(annotationItem)
                    .setContent(content)
                    .setSettersAvailable(useSetters)
                    .setGettersAvailable(useGetters)
                    .setRootClassName(className)
                    .setRewriteClasses(rewriteClasses)
                    .build());

        } catch (RoboPluginException exception) {
            messageService.onPluginExceptionHandled(exception);
        }
    }


    /**
     * Disable autovalue when select Kotlin
     *
     * @param languageItem The selected language item
     */
    private void changeAnnotation(LanguageItem languageItem) {

        ButtonGroup                 buttonGroup = generatorVew.getTypeButtonGroup();
        Enumeration<AbstractButton> buttons     = buttonGroup.getElements();

        if (languageItem.equals(LanguageItem.KOTLIN_DTO)) {
            while (buttons.hasMoreElements()) {
                final AbstractButton button = buttons.nextElement();

                if (button.getText().contains("AutoValue")) {
                    button.setSelected(false);
                    button.setEnabled(false);
                }
                else if (button.getText().equals(AnnotationItem.NONE.getText())) {
                    button.setSelected(true);
                }
            }
        }
        else {
            while (buttons.hasMoreElements()) {
                final AbstractButton button = buttons.nextElement();

                if (!button.isEnabled()) {
                    button.setEnabled(true);
                }
            }
        }
    }


    private LanguageItem resolveLanguageItem() {

        final ButtonGroup buttonGroup = generatorVew.getLanguageGroup();

        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons
                .hasMoreElements(); ) {
            final AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                for (LanguageItem languageItem : LanguageItem.values()) {
                    if (languageItem.getText().equals(button.getText())) {
                        return languageItem;
                    }
                }
            }
        }

        return LanguageItem.POJO;
    }


    private AnnotationItem resolveAnnotationItem() {

        final ButtonGroup buttonGroup = generatorVew.getTypeButtonGroup();
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons
                .hasMoreElements(); ) {
            final AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                for (AnnotationItem annotationItem : AnnotationItem.values()) {
                    if (annotationItem.getText().equals(button.getText())) {
                        return annotationItem;
                    }
                }
            }
        }
        return AnnotationItem.NONE;
    }
}
