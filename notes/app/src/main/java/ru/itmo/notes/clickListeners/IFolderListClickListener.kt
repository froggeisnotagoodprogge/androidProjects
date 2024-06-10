package ru.itmo.notes.clickListeners

import androidx.cardview.widget.CardView
import ru.itmo.notes.models.Folder

interface IFolderListClickListener {
    fun onClick(folder: Folder)

    fun onLongClick(folder: Folder, view: CardView)

    fun expandFolderClick(folder: Folder)

    fun deleteFolderClick(folder: Folder)
}