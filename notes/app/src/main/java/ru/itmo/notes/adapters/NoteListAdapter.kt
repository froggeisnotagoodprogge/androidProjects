package ru.itmo.notes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.itmo.notes.R
import ru.itmo.notes.clickListeners.INoteListClickListener
import ru.itmo.notes.models.Note

class NoteListAdapter(
    private val context: Context,
    private val list: MutableList<Note>,
    private val listener: INoteListClickListener
) : RecyclerView.Adapter<NoteListAdapter.NotesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notes_list, parent, false)
        return NotesViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        with(holder) {
            val currentNote = list[position]
            noteTitle.text = currentNote.title
            noteText.text = currentNote.text

            deleteButton.setOnClickListener {
                listener.deleteNoteClick(currentNote)
            }

            notesLayout.setOnClickListener {
                listener.onClick(currentNote)
            }

            notesLayout.setOnLongClickListener {
                listener.onLongClick(currentNote, notesLayout)
                true
            }
        }
    }

    class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notesLayout: androidx.cardview.widget.CardView = itemView.findViewById(R.id.notes_layout)
        val noteTitle: TextView = itemView.findViewById(R.id.noteListTitle)
        val noteText: TextView = itemView.findViewById(R.id.noteListText)
        val deleteButton: ImageButton = itemView.findViewById(R.id.noteListBin)
    }
}
