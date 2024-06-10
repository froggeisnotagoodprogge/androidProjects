package ru.itmo.notes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Room
import ru.itmo.notes.adapters.BinFolderListAdapter
import ru.itmo.notes.adapters.BinNoteListAdapter
import ru.itmo.notes.adapters.FolderListAdapter
import ru.itmo.notes.clickListeners.IBinFolderListClickListener
import ru.itmo.notes.clickListeners.IBinNoteListClickListener
import ru.itmo.notes.clickListeners.IFolderListClickListener
import ru.itmo.notes.clickListeners.INoteListClickListener
import ru.itmo.notes.database.FolderDB
import ru.itmo.notes.database.NoteDB
import ru.itmo.notes.models.Folder
import ru.itmo.notes.models.Note

class MainActivity : ComponentActivity() {
    private var notes: MutableList<Note> = ArrayList()
    private var folders: MutableList<Folder> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var folderListAdapter: FolderListAdapter
    private lateinit var databaseNotes: NoteDB
    private lateinit var databaseFolders: FolderDB
    private lateinit var tmp: Context
    private var isRedact: Boolean = false
    private var objectToRedact: Int = 0

    private val folderListClickListener = object : IFolderListClickListener {

        override fun onClick(folder: Folder) {
            val title = folder.title
            setContentView(R.layout.add_folder)
            val titleObj: EditText = findViewById(R.id.folderTitle)
            titleObj.setText(title)
            isRedact = true
            objectToRedact = folder.id
        }

        override fun onLongClick(folder: Folder, view: CardView) {
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun deleteFolderClick(folder: Folder) {
            moveFolderToBin(folder)
            recyclerView = findViewById(R.id.recyclerView)
            folders = databaseFolders.folderDAO().getAll()
            folderListAdapter.notifyDataSetChanged()
            updateMainView()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun expandFolderClick(folder: Folder) {
            folders = databaseFolders.folderDAO().getAll()
            updateMainView()
            val folderId = folder.id
            if (databaseFolders.folderDAO().getById(folderId)?.isExpanded == true) {
                databaseFolders.folderDAO().setExpandedFalse(folderId)
            } else {
                databaseFolders.folderDAO().setExpandedTrue(folderId)
            }
            folders = databaseFolders.folderDAO().getAll()
            updateMainView()
        }
    }

    private val binFolderListClickListener = object : IBinFolderListClickListener {

        @SuppressLint("NotifyDataSetChanged")
        override fun restoreFolderClick(folder: Folder) {
            restoreFolderFromBin(folder)
            updateBinView()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun deleteFolderClick(folder: Folder) {
            deleteFolderFromBin(folder)
            updateBinView()
        }
    }

    private val noteListClickListener = object : INoteListClickListener {
        override fun onClick(note: Note) {
            val title = note.title
            val text = note.text
            val folderId = note.folderId
            setContentView(R.layout.add_note)
            val titleObj: EditText = findViewById(R.id.noteTitle)
            val textObj: EditText = findViewById(R.id.noteText)
            val spinner: Spinner = findViewById(R.id.static_spinner)
            titleObj.setText(title)
            textObj.setText(text)
            val adapter: ArrayAdapter<String> =
                ArrayAdapter(tmp, R.layout.spinner_item, folders.map { x -> x.title })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(adapter.getPosition(databaseFolders.folderDAO().getById(folderId)?.title))
            isRedact = true
            objectToRedact = note.id
        }

        override fun onLongClick(note: Note, view: CardView) {
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun deleteNoteClick(note: Note) {
            moveToBin(note)
            recyclerView = findViewById(R.id.recyclerView)
            notes = databaseNotes.noteDAO().getAll()
            folderListAdapter.notifyDataSetChanged()
            updateMainView()
        }
    }

    private val binNoteListClickListener = object : IBinNoteListClickListener {

        override fun restoreNoteClick(note: Note) {
            restoreNoteFromBin(note)
            updateBinView()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun deleteNoteClick(note: Note) {
            deleteNoteFromBin(note)
            updateBinView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        recyclerView = findViewById(R.id.recyclerView)

        val nameN = "Notes DB"
        val nameF = "Folders DB"

        tmp = this
        databaseNotes =
            Room.databaseBuilder(this.applicationContext, NoteDB::class.java, nameN)
                .allowMainThreadQueries().fallbackToDestructiveMigration().build()
        databaseFolders =
            Room.databaseBuilder(this.applicationContext, FolderDB::class.java, nameF)
                .allowMainThreadQueries().fallbackToDestructiveMigration().build()

        initializeMainData()
    }

    private fun initializeMainData() {
        if (databaseFolders.folderDAO().getByTitle("Main") == null) {
            val folderTmp = Folder()
            folderTmp.title = "Main"
            folderTmp.pos = databaseFolders.folderDAO().getAll().size
            folderTmp.isDeleted = false
            folderTmp.isExpanded = true
            databaseFolders.folderDAO().insert(folderTmp)
        }

        notes = databaseNotes.noteDAO().getAll()
        folders = databaseFolders.folderDAO().getAll()
        updateMainView()
    }

    private fun updateMainView() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        folderListAdapter =
            FolderListAdapter(tmp, folders, folderListClickListener, databaseNotes, noteListClickListener)
        val notesTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.DOWN or ItemTouchHelper.UP,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                moveFolderPosition(viewHolder, target)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        })

        recyclerView.adapter = folderListAdapter
        notesTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun goToBin(view: View) {
        setContentView(R.layout.bin_page)
        updateBinView()
    }

    private fun updateBinView() {
        val recyclerViewNote: RecyclerView = findViewById(R.id.recyclerViewNotes)
        val recyclerViewFolder: RecyclerView = findViewById(R.id.recyclerViewFolders)
        recyclerViewFolder.setHasFixedSize(true)
        recyclerViewFolder.layoutManager =
            StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        val folderListAdapterBin = BinFolderListAdapter(
            tmp,
            databaseFolders.folderDAO().getAllBin(),
            binFolderListClickListener,
            databaseNotes,
            binNoteListClickListener
        )
        recyclerViewFolder.adapter = folderListAdapterBin

        val notesBinTmp = databaseNotes.noteDAO().getAllBin()
        val notesBin: MutableList<Note> = ArrayList()
        for (noteTmp in notesBinTmp) {
            if (databaseFolders.folderDAO().getById(noteTmp.folderId) != null) {
                if (!databaseFolders.folderDAO().getById(noteTmp.folderId)!!.isDeleted)
                    notesBin.add(noteTmp)
            }
        }
        recyclerViewNote.setHasFixedSize(true)
        recyclerViewNote.layoutManager =
            StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        val binNoteListAdapter = BinNoteListAdapter(tmp, notesBin, binNoteListClickListener)
        recyclerViewNote.adapter = binNoteListAdapter
    }

    fun goToAddNote(view: View) {
        setContentView(R.layout.add_note)
        val staticSpinner: Spinner = findViewById(R.id.static_spinner)
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(tmp, R.layout.spinner_item, folders.map { x -> x.title })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        staticSpinner.adapter = adapter
    }

    fun goToAddFolder(view: View) {
        setContentView(R.layout.add_folder)
    }

    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    fun createFolder(view: View) {
        val title: EditText = findViewById(R.id.folderTitle)
        val titleStr: String = title.text.toString()

        if (title.text.isEmpty()) {
            showAlertDialog("Warn", "Title of folder must be initialized!")
            return
        }

        if (isRedact) {
            updateExistingFolder(titleStr)
        } else {
            createNewFolder(titleStr)
        }

        setContentView(R.layout.main_activity)
        recyclerView = findViewById(R.id.recyclerView)
        folders = databaseFolders.folderDAO().getAll()
        folderListAdapter.notifyDataSetChanged()
        updateMainView()
        isRedact = false
    }

    private fun updateExistingFolder(titleStr: String) {
        if (databaseFolders.folderDAO().getByTitle(titleStr) != null) {
            showAlertDialog("Warn", "Title of folder must be unique!")
        } else {
            databaseFolders.folderDAO().update(objectToRedact, titleStr)
        }
    }

    private fun createNewFolder(titleStr: String) {
        if (databaseFolders.folderDAO().getByTitle(titleStr) != null) {
            showAlertDialog("Warn", "Title of folder must be unique!")
        } else {
            val folder = Folder()
            folder.title = titleStr
            folder.isDeleted = false
            folder.pos = folders.size
            folder.isExpanded = true

            databaseFolders.folderDAO().insert(folder)
        }
    }

    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    fun createNote(view: View) {
        val title: EditText = findViewById(R.id.noteTitle)
        val content: EditText = findViewById(R.id.noteText)
        val spinner: Spinner = findViewById(R.id.static_spinner)
        val titleStr: String = title.text.toString()
        var textStr: String = content.text.toString()
        val folderName: String = (spinner.selectedItem as String)

        if (title.text.isEmpty()) {
            showAlertDialog("Warn", "Title of note must be initialized!")
            return
        }

        if (isRedact) {
            updateExistingNote(titleStr, textStr, folderName)
        } else {
            createNewNote(titleStr, textStr, folderName)
        }

        setContentView(R.layout.main_activity)
        recyclerView = findViewById(R.id.recyclerView)
        notes = databaseNotes.noteDAO().getAll()
        folderListAdapter.notifyDataSetChanged()
        updateMainView()
        isRedact = false
    }

    private fun updateExistingNote(titleStr: String, textStr: String, folderName: String) {
        databaseNotes.noteDAO().update(
            objectToRedact,
            titleStr,
            textStr,
            databaseFolders.folderDAO().getByTitle(folderName)?.id ?: -1
        )
    }

    private fun createNewNote(titleStr: String, textStr: String, folderName: String) {
        val note = Note()
        note.title = titleStr
        note.text = textStr
        note.isDeleted = false
        note.folderId = databaseFolders.folderDAO().getByTitle(folderName)?.id ?: -1
        note.pos = notes.size
        databaseNotes.noteDAO().insert(note)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun cancel(view: View) {
        setContentView(R.layout.main_activity)
        recyclerView = findViewById(R.id.recyclerView)
        folders = databaseFolders.folderDAO().getAll()
        folderListAdapter.notifyDataSetChanged()
        updateMainView()
        isRedact = false
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .create()
            .show()
    }

    private fun moveFolderToBin(folder: Folder) {
        databaseFolders.folderDAO().moveToBin(folder.id)
        databaseNotes.noteDAO().moveChildrenToBin(folder.id)
        recyclerView = findViewById(R.id.recyclerView)
        folders = databaseFolders.folderDAO().getAll()
        folderListAdapter.notifyDataSetChanged()
        updateMainView()
    }

    private fun restoreFolderFromBin(folder: Folder) {
        databaseNotes.noteDAO().restoreChildrenFromBin(folder.id)
        databaseFolders.folderDAO().restoreFromBin(folder.id)
        updateBinView()
    }

    private fun deleteFolderFromBin(folder: Folder) {
        for (note in databaseNotes.noteDAO().getAllBinByFolder(folder.id))
            databaseNotes.noteDAO().delete(note)

        databaseFolders.folderDAO().delete(folder)
        updateBinView()
    }

    private fun moveFolderPosition(viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) {
        val initial = viewHolder.adapterPosition
        val final = target.adapterPosition
        val firstNote = folders[initial]
        val secondNote = folders[final]
        databaseFolders.folderDAO().changePos(firstNote.id, final)
        databaseFolders.folderDAO().changePos(secondNote.id, initial)
        folderListAdapter.notifyItemMoved(initial, final)
    }

    private fun moveToBin(note: Note) {
        databaseNotes.noteDAO().moveToBin(note.id)
        recyclerView = findViewById(R.id.recyclerView)
        notes = databaseNotes.noteDAO().getAll()
        folderListAdapter.notifyDataSetChanged()
        updateMainView()
    }

    private fun restoreNoteFromBin(note: Note) {
        if (databaseFolders.folderDAO().getById(note.folderId)?.isDeleted == true) {
            databaseNotes.noteDAO().update(
                note.id,
                note.title,
                note.text,
                databaseFolders.folderDAO().getByTitle("Main")?.id ?: -1
            )
            databaseNotes.noteDAO().restoreFromBin(note.id)
        } else {
            databaseNotes.noteDAO().restoreFromBin(note.id)
        }
        updateBinView()
    }

    private fun deleteNoteFromBin(note: Note) {
        databaseNotes.noteDAO().delete(note)
        updateBinView()
    }
}