package ru.itmo.notes.clickListeners

import ru.itmo.notes.models.Note

interface IBinNoteListClickListener {

    fun restoreNoteClick(note: Note)

    fun deleteNoteClick(note: Note)
}