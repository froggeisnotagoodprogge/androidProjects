package ru.itmo.notes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ru.itmo.notes.R
import ru.itmo.notes.clickListeners.IFolderListClickListener
import ru.itmo.notes.clickListeners.INoteListClickListener
import ru.itmo.notes.database.NoteDB
import ru.itmo.notes.models.Folder
import ru.itmo.notes.models.Note

class FolderListAdapter(
    private val context: Context,
    private val list: MutableList<Folder>,
    private val listener: IFolderListClickListener,
    private val databaseNote: NoteDB,
    private val existNoteListClickListener: INoteListClickListener
) : RecyclerView.Adapter<FolderListAdapter.FoldersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersViewHolder {
        return FoldersViewHolder(
            LayoutInflater.from(context).inflate(R.layout.folder_list, parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: FoldersViewHolder, position: Int) {
        with(holder) {
            noteTitle.text = list[position].title
            val notes: MutableList<Note> = databaseNote.noteDAO().getAllByFolder(list[position].id)

            expandButton.setOnClickListener {
                listener.expandFolderClick(list[position])
            }

            if (list[position].title != "Main") {
                deleteButton.setOnClickListener {
                    listener.deleteFolderClick(list[layoutPosition])
                }
                notesLayout.setOnClickListener {
                    listener.onClick(list[layoutPosition])
                }
            } else {
                deleteButton.visibility = View.INVISIBLE
            }

            notesLayout.setOnLongClickListener {
                listener.onLongClick(list[layoutPosition], notesLayout)
                true
            }

            recyclerView.visibility = if (list[position].isExpanded) View.VISIBLE else View.INVISIBLE

            if (list[position].isExpanded) {
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
                val noteListAdapter = NoteListAdapter(context, notes, existNoteListClickListener)
                val notesTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val initial = viewHolder.adapterPosition
                        val final = target.adapterPosition
                        val firstNote = notes[initial]
                        val secondNote = notes[final]

                        databaseNote.noteDAO().changePos(firstNote.id, final)
                        databaseNote.noteDAO().changePos(secondNote.id, initial)
                        noteListAdapter.notifyItemMoved(initial, final)

                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        // Swiped handling (if needed)
                    }
                })
                recyclerView.adapter = noteListAdapter
                notesTouchHelper.attachToRecyclerView(recyclerView)
            }
        }
    }

    class FoldersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notesLayout: androidx.cardview.widget.CardView = itemView.findViewById(R.id.folders_layout)
        val noteTitle: TextView = itemView.findViewById(R.id.folderListTitle)
        val deleteButton: ImageButton = itemView.findViewById(R.id.folderListBin)
        val expandButton: ImageButton = itemView.findViewById(R.id.folderListExpand)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.noteRecyclerView)
    }
}