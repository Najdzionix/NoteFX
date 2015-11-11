package com.kn.elephant.note.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by Kamil Nadłonek on 11.11.15.
 * email:kamilnadlonek@gmail.com
 */
@Data
@DatabaseTable(tableName = "note")
public class Note {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(columnName = "createAt", persisterClass = DateTimePersister.class)
    private LocalDateTime createAt;

    @DatabaseField(columnName = "updateAt", persisterClass = DateTimePersister.class)
    private LocalDateTime updateAt;

    @DatabaseField(columnName = "title")
    private String title;

    @DatabaseField(columnName = "shortDesc")
    private String shortDescription;

    @DatabaseField(columnName = "content")
    private String content;

    @DatabaseField(columnName = "cleanContent")
    private String cleanContent;

    @DatabaseField(columnName = "deleted")
    private boolean deleted;

    @DatabaseField(columnName = "type")
    private NoteType type;

    @DatabaseField(foreign = true, columnName = "parentId")
    private Note parent;

    public Note() {};
}
