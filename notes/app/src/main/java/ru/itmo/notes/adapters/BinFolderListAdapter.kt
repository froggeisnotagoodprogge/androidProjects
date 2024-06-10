package ru.itmo.notes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ru.itmo.notes.R
import ru.itmo.notes.clickListeners.IBinFolderListClickListener
import ru.itmo.notes.clickListeners.IBinNoteListClickListener
import ru.itmo.notes.database.NoteDB
import ru.itmo.notes.models.Folder

class BinFolderListAdapter(
    private val context: Context,
    private val list: MutableList<Folder>,
    private val listener: IBinFolderListClickListener,
    private val databaseNote: NoteDB,
    private val binNoteListClickListener: IBinNoteListClickListener
) : RecyclerView.Adapter<BinFolderListAdapter.FoldersViewHolderBin>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersViewHolderBin {
        return FoldersViewHolderBin(
            LayoutInflater.from(context).inflate(R.layout.folder_list_bin, parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: FoldersViewHolderBin, position: Int) {
        with(holder) {
            noteTitle.text = list[position].title

            restoreButton.setOnClickListener {
                listener.restoreFolderClick(list[layoutPosition])
            }

            deleteButton.setOnClickListener {
                listener.deleteFolderClick(list[layoutPosition])
            }

            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
            recyclerView.adapter = BinNoteListAdapter(context, databaseNote.noteDAO().getAllBinByFolder(list[position].id), binNoteListClickListener)
        }
    }

    class FoldersViewHolderBin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notesLayout: androidx.cardview.widget.CardView = itemView.findViewById(R.id.folders_layout)
        val noteTitle: TextView = itemView.findViewById(R.id.folderListTitle)
        val deleteButton: ImageButton = itemView.findViewById(R.id.folderListBin)
        val restoreButton: ImageButton = itemView.findViewById(R.id.folderListRestore)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.noteRecyclerView)
    }
}
