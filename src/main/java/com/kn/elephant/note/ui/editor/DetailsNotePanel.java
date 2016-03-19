package com.kn.elephant.note.ui.editor;

import com.google.inject.Inject;
import com.kn.elephant.note.dto.NoteDto;
import com.kn.elephant.note.dto.NoticeData;
import com.kn.elephant.note.dto.TagDto;
import com.kn.elephant.note.service.TagService;
import com.kn.elephant.note.ui.BasePanel;
import com.kn.elephant.note.ui.EditableLabel;
import com.kn.elephant.note.ui.Icons;
import com.kn.elephant.note.ui.TagNode;
import com.kn.elephant.note.utils.ActionFactory;
import com.kn.elephant.note.utils.TagStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.GridView;
import org.controlsfx.control.action.ActionMap;
import org.controlsfx.control.action.ActionProxy;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;
import org.controlsfx.validation.decoration.ValidationDecoration;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Kamil Nadłonek on 09.11.15.
 * email:kamilnadlonek@gmail.com
 */
public class DetailsNotePanel extends BasePanel {
    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm");
    private static final Logger LOGGER = LogManager.getLogger(DetailsNotePanel.class);

    private NoteDto noteDto;
    private GridPane gridPane;
    private ObservableList<TagDto> tagDtos;
    private TextField tagTF;

    private TagDto autoCompelteTag = null;

    @Inject
    private TagService tagService;
    private ValidationSupport validationSupport;

    public DetailsNotePanel() {
        ActionMap.register(this);
        setMaxHeight(150);
    }

    public void loadNote(NoteDto noteDto) {
        this.noteDto = noteDto;
        setLeft(createLeftPanel());
        setCenter(createTagPanel());
    }

    private Node createLeftPanel() {
        BorderPane box = new BorderPane();
        gridPane = new GridPane();
        createDates("Created:", 0);
        createDates("Updated:", 2);
        box.setTop(gridPane);
        box.setCenter(new EditableLabel(noteDto.getTitle()));

        return box;
    }

    private void createDates(String labelText, int colIndex) {
        Label label = new Label(labelText);
        label.getStyleClass().add("noteLabelTime");
        GridPane.setHalignment(label, HPos.RIGHT);
        GridPane.setConstraints(label, colIndex, 0);

        Label timeLabel = new Label(noteDto.getCreateAt().format(FORMATTER));
        timeLabel.getStyleClass().add("noteDateTime");
        GridPane.setHalignment(timeLabel, HPos.LEFT);
        GridPane.setConstraints(timeLabel, colIndex + 1, 0);

        gridPane.getChildren().addAll(label, timeLabel);
    }

    private Node createTagPanel() {
        BorderPane content = new BorderPane();
        List<TagDto> noteTags = tagService.getTagByNoteId(noteDto.getId());
        tagDtos = FXCollections.observableList(noteTags);
        GridView<TagDto> gridView = new GridView(tagDtos);
        gridView.setCellFactory(arg0 -> new TagNode("removeTag"));
        gridView.setCellWidth(100);
        gridView.setCellHeight(25);
        content.setCenter(gridView);

        HBox box = new HBox();
        box.setSpacing(5);
        Label addTagLabel = new Label("Add tag:");
        tagTF = new TextField();
        box.getStyleClass().add("textFieldTag");
        validationSupport = new ValidationSupport();
        ValidationDecoration cssDecorator = new StyleClassValidationDecoration();
        validationSupport.setValidationDecorator(cssDecorator);
        validationSupport.registerValidator(tagTF, Validator.createEmptyValidator("Name tag can not be empty!"));

        initAutoCompleteForTags(noteTags);

        Button addTagButton = ActionUtils.createButton(ActionFactory.getAddTag());
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(spacer, addTagLabel, tagTF, addTagButton);
        content.setBottom(box);
        return content;
    }

    private void initAutoCompleteForTags(List<TagDto> tags) {
        List<TagDto> freeTags = tagService.getAll().stream().filter(tag -> !tags.contains(tag)).collect(Collectors.toList());

        AutoCompletionBinding<TagDto> bind = TextFields.bindAutoCompletion(tagTF,
                param -> freeTags.stream().filter(tagDto -> tagDto.getName().toLowerCase(Locale.getDefault()).contains(param.getUserText().toLowerCase(Locale.getDefault()))).collect(Collectors.toList()),
                new TagStringConverter());

        bind.setOnAutoCompleted(event -> {
            autoCompelteTag = event.getCompletion();
        });
    }

    @ActionProxy(text = "")
    protected void removeTag(ActionEvent event) {
        LOGGER.info("remove tag action");
//        todo find better way get item.
        TagDto item = ((TagNode) ((Button) event.getSource()).getParent().getParent()).getItem();
        LOGGER.debug(item);
        boolean isDeleted = tagService.removeTagFromNote(item.getId(), noteDto.getId());
        if (isDeleted) {
            tagDtos.remove(item);
        } else {
            ActionFactory.callAction("showNotificationPanel", new NoticeData("Operation remove tag failed", Icons
                    .ERROR));
        }
    }

    @ActionProxy(text = "")
    private void addTag() {
        LOGGER.info(String.format("Add tag of name %s", tagTF.getText()));
        if(validationSupport.isInvalid()) {
            return;
        }
        TagDto dto;
        if (autoCompelteTag != null && autoCompelteTag.getName().equalsIgnoreCase(tagTF.getText().trim())) {
            dto = autoCompelteTag;
        } else {
            dto = new TagDto();
            dto.setName(tagTF.getText());
        }
        dto.getNotes().add(noteDto);
        Optional<TagDto> tagDto = tagService.saveTag(dto);
        if (tagDto.isPresent()) {
            tagDtos.add(tagDto.get());
            tagTF.clear();
        } else {
            ActionFactory.callAction("showNotificationPanel", new NoticeData("Operation add tag failed", Icons.ERROR));

        }
    }
}
