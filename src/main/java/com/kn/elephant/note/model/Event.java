package com.kn.elephant.note.model;

import java.time.LocalDateTime;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

/**
 * Created by Kamil Nadłonek on 20-08-2017
 * email:kamilnadlonek@gmail.com
 */
@Data
@DatabaseTable(tableName = "event")
public class Event {

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(persisterClass = DateTimePersister.class, canBeNull = false)
	private LocalDateTime startDate;

	@DatabaseField
	private String name;

	@DatabaseField
	private String repeat;

	@DatabaseField
	private String content;

	@DatabaseField(canBeNull = false, defaultValue = "false")
	private boolean deleted;


}
