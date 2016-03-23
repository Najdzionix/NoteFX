package com.kn.elephant.note.ui.editor;

import com.google.inject.Inject;
import com.kn.elephant.note.dto.NoteDto;
import com.kn.elephant.note.dto.NoticeData;
import com.kn.elephant.note.service.NoteService;
import com.kn.elephant.note.ui.BasePanel;
import com.kn.elephant.note.utils.Icons;
import com.kn.elephant.note.utils.ActionFactory;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import lombok.extern.log4j.Log4j2;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionMap;
import org.controlsfx.control.action.ActionProxy;
import org.controlsfx.control.action.ActionUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;

/**
 * Created by Kamil Nadłonek on 10.11.15.
 * email:kamilnadlonek@gmail.com
 */
@Log4j2
public class NotePanel extends BasePanel {

    private NoteDto currentNoteDto;
    private DetailsNotePanel detailsNotePanel;
    private HTMLEditor editor;
    private WebView webView;
    private Button insertLinkButton;

    @Inject
    private NoteService noteService;
    private NotificationPane notificationPane;

    public NotePanel(NoteDto noteDto) {
        ActionMap.register(this);
        this.currentNoteDto = noteDto;
        detailsNotePanel = new DetailsNotePanel();
        setTop(detailsNotePanel);
        editor = new HTMLEditor();
        webView = new WebView();
        setPadding(new Insets(15));
        notificationPanel();
        addButtonsToToolbar();
    }

    private void notificationPanel() {
        notificationPane = new NotificationPane();
        notificationPane.showFromTopProperty().setValue(false);
        notificationPane.getStyleClass().add("notification");
        setCenter(notificationPane);
    }

    @ActionProxy(text = "loadnote")
    private void loadNote(ActionEvent event) {
        notificationPane.setContent(editor);
        currentNoteDto = (NoteDto) event.getSource();
        log.debug("Load note: " + currentNoteDto);
        detailsNotePanel.loadNote(currentNoteDto);
        editor.setHtmlText("");
        editor.setHtmlText(currentNoteDto.getContent());
    }

    private void addButtonsToToolbar() {
        Node node = editor.lookup(".top-toolbar");
        if (node instanceof ToolBar) {
            createButtons((ToolBar) node);
        }
    }

    private void createButtons(ToolBar toolBar) {
        Action saveAction = ActionMap.action("saveNote");
        final String sizeIcon = "1.4em";
        Icons.addIcon(MaterialDesignIcon.CONTENT_SAVE, saveAction, sizeIcon);
        Button saveButton = ActionUtils.createButton(saveAction);

        Action removeAction = ActionMap.action("removeNote");
        Icons.addIcon(MaterialDesignIcon.DELETE, removeAction, sizeIcon);
        Button removeButton = ActionUtils.createButton(removeAction);

        Action insertAction = ActionMap.action("insertLink");
        Icons.addIcon(MaterialDesignIcon.LINK, insertAction, sizeIcon);
        insertLinkButton =  new Button();
        ActionUtils.configureButton(insertAction, insertLinkButton);
        
        editor.setOnMouseClicked((MouseEvent event) -> {
            final int clickCount = event.getClickCount();
            if (clickCount == 2) {
                insertLinkButton.getStyleClass().remove("disableButton");
            }
        });

        toolBar.getItems().addAll(saveButton, new Separator(), removeButton, new Separator(), insertLinkButton);
    }


    private void httpLisener() {
        editor.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<InputEvent>() {
            Pattern urlPattern = Pattern.compile("http://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");

            @Override
            public void handle(InputEvent event) {
                log.info("event ");
                log.info(editor.getHtmlText());
                String text = editor.getHtmlText();
                Matcher matcher = urlPattern.matcher(text);
                // check all occurance
                boolean foundUrl = false;
                while (matcher.find()) {
                    System.out.print("Start index: " + matcher.start());
                    System.out.print(" End index: " + matcher.end() + " ");
                    System.out.println(matcher.group());
                    text.replace(matcher.group(), "<url>" + matcher.group() + "</url>");
                    foundUrl = true;
                }
//                if(foundUrl) {
//                    editor.setHtmlText(text);
//                }
            }
        });

    }

    @ActionProxy(text = "")
    private void insertLink(ActionEvent event) {
        log.debug("Insert link");
        WebView webView = (WebView) editor.lookup("WebView");
        String selected = (String) webView.getEngine().executeScript("window.getSelection().toString();");
        String currentText = editor.getHtmlText();
        String hyperlinkHtml = "<a href=\"" + selected.trim() + "\" title=\"" + selected + "\" target=\"_blank\">" + selected + "</a>";

        if (selected != null & !selected.isEmpty()) {
            editor.setHtmlText(currentText.replace(selected, hyperlinkHtml));
        }
        insertLinkButton.getStyleClass().add("disableButton");
    }

    @ActionProxy(text = "")
    private void updateTitle(ActionEvent event) {
        log.debug("Update note title" + event.getSource());
        currentNoteDto.setTitle((String) event.getSource());
    }

    @ActionProxy(text = "")
    private void saveNote(ActionEvent event) {
//        todo load title
        currentNoteDto.setContent(editor.getHtmlText());
        log.debug("Save note:" + currentNoteDto);
        Optional<NoteDto> updatedNote = noteService.saveNote(currentNoteDto);
        if (updatedNote.isPresent()) {
            currentNoteDto = updatedNote.get();
            loadNote(new ActionEvent(currentNoteDto, null));
            ActionFactory.callAction("showNotificationPanel", new NoticeData("Note saved."));
        } else {
            ActionFactory.callAction("showNotificationPanel", NoticeData.createErrorNotice("Operation saving failed"));
        }
    }

    @ActionProxy(text = "")
    private void removeNote(ActionEvent event) {
        log.debug("Remove note:" + currentNoteDto);
        if (noteService.removeNote(currentNoteDto.getId())) {
            log.info("note was removed");
            ActionFactory.callAction("removeNoteFromList");
            ActionFactory.callAction("showNotificationPanel", new NoticeData("Note has been removed."));
        }
    }

    @ActionProxy(text = "Edit mode")
    private void switchDisplayMode(ActionEvent event) {
        log.debug("Switch mode display note");
        if (((ToggleButton) event.getSource()).isSelected()) {
            editor.setHtmlText(currentNoteDto.getContent());
            notificationPane.setContent(editor);
        } else {
            webView.getEngine().loadContent(currentNoteDto.getContent());
            webView.setContextMenuEnabled(false);
            webView.setDisable(true);
            webView.addEventFilter(KeyEvent.ANY, KeyEvent::consume);
            notificationPane.setContent(webView);
        }
    }

    @ActionProxy(text = "")
    private void showNotificationPanel(ActionEvent event) throws InterruptedException {
        NoticeData noticeData = (NoticeData) event.getSource();
        notificationPane.show(noticeData.getMessage());
        notificationPane.setGraphic(noticeData.getIcon());
        hideNotifications(4000);
    }

    private void hideNotifications(int time) {
        Runnable task = () -> {
            try {
                Thread.sleep(time);
                notificationPane.hide();
            } catch (InterruptedException e) {
                log.error("Hide notification thread error", e);
            }
        };
        new Thread(task).start();
    }
}