package ru.itmo.notes.clickListeners

import ru.itmo.notes.models.Folder

interface IBinFolderListClickListener {

    fun restoreFolderClick(folder: Folder)

    fun deleteFolderClick(folder: Folder)
}