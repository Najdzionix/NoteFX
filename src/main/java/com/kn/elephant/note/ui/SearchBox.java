package com.kn.elephant.note.ui;

import com.google.inject.Inject;
import com.kn.elephant.note.service.NoteService;
import com.kn.elephant.note.ui.control.ListNotesControl;
import com.kn.elephant.note.utils.ActionFactory;
import com.kn.elephant.note.utils.Icons;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionMap;
import org.controlsfx.control.action.ActionProxy;
import org.controlsfx.control.action.ActionUtils;

/**
 * Created by Kamil Nadłonek on 29.10.15.
 * email:kamilnadlonek@gmail.com
 */
@Log4j2
public class SearchBox extends BasePanel {

    private TextField textBox;
    private Button clearButton;

    @Inject
    private NoteService noteService;

    public SearchBox() {
        ActionMap.register(this);
        setId("SearchBox");
        getStyleClass().add("search-box");
        setMinHeight(24);
        setPrefSize(200, 24);
        setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
        textBox = new TextField();
        textBox.setPromptText("Search");
        Action clearAction = ActionFactory.getAction("clearAction");
        clearButton = ActionUtils.createButton(clearAction);
        Icons.addIcon(OctIcon.X, clearAction, "1.0em");
        clearButton.setVisible(false);

        ListNotesControl notesControl = new ListNotesControl("Results");
        notesControl.setOwner(textBox);
        getChildren().addAll(textBox, clearButton);

        textBox.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            clearButton.setVisible(StringUtils.isNotEmpty(newValue));
            if (StringUtils.isNotEmpty(newValue)) {
                notesControl.showNotes(noteService.findNotes(newValue));
            }
        });
    }

    @Override
    protected void layoutChildren() {
        textBox.resize(getWidth(), getHeight());
        clearButton.resizeRelocate(getWidth() - 18, 6, 12, 13);
    }

    @ActionProxy(text = "")
    private void clearAction() {
        textBox.setText("");
        textBox.requestFocus();
    }
}
