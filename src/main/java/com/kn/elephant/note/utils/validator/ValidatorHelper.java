package com.kn.elephant.note.utils.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

/**
 * Created by Kamil Nadłonek on 19.03.16.
 * email:kamilnadlonek@gmail.com
 */
public class ValidatorHelper {
    private static final String ERROR_CSS = "error";
    private Map<Node, Boolean> fields = new HashMap<>();

    public void registerEmptyValidator(Node node, String message) {
        if (node instanceof TextField) {
            TextField tf = ((TextField) node);
            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                setValidationResult(tf, message, StringUtils.isNotEmpty(tf.getText()));
            });

            tf.focusedProperty().addListener((observable, oldValue, newValue) -> {
//                    loses focus
                if (!newValue) {
                    setValidationResult(tf, message, StringUtils.isNotBlank(tf.getText()));
                }
            });
        }

        fields.put(node, true);
    }

    /**
     * Register provide by user validator and it is fire after lose focus on node.
     *
     * @param node Node to validation
     * @param message Error message
     * @param validator Object which validate node
     */
    public void registerCustomValidator(Node node, String message, Validator validator) {
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                runValidator(node, message, validator);
            }
        });

    }

    private void runValidator(Node node, String message, Validator validator) {
        boolean isValid = validator.validate(node);
        setValidationResult(node, message, isValid);
    }

    /**
     *  Register given validator and it is fire after change text property.
     * @param node control to validation
     * @param message error message
     * @param validator validator which check control
     */
    public void registerCustomValidatorChangeText(TextInputControl node, String message, Validator validator) {
        node.textProperty().addListener((observable, oldValue, newValue) -> {
            runValidator(node, message, validator);
        });
        runValidator(node, message, validator);
    }

    private void setValidationResult(Node node, String message, boolean isValid) {
        if (!isValid) {
            Tooltip tip = new Tooltip(message);
            tip.getStyleClass().add("tooltipError");
            Tooltip.install(node, tip);
            node.getStyleClass().add(ERROR_CSS);
            fields.put(node, false);
        } else {
            Tooltip.uninstall(node, null);
            node.getStyleClass().remove(ERROR_CSS);
            fields.put(node, true);
        }
    }

    public boolean isValid() {
        long count = fields.values().stream().filter(b -> !b).count();
        return count == 0;
    }

    /**
     * Clear all results of validation.
     */
    public void removeAllNodes(){
        fields.clear();
    }

}
