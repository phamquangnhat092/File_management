package com.example.filemanager

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files.createFile


class MainActivity : AppCompatActivity() {
    lateinit var path: String
    lateinit var listView: ListView
    var filesAndFolders: Array<File>? = null
    var PREF_FILE="FILE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= 30) {
            if (Environment.isExternalStorageManager() == false) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission denied => request permission")
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
            } else
                Log.v("TAG", "Permission granted")
        }
        listView = findViewById<ListView>(R.id.list_view)


        var mypath = intent.getStringExtra("path")
//        Log.v("mypath", )
        if (mypath.isNullOrEmpty()) path = Environment.getExternalStorageDirectory().path
        else path = mypath
        hadleGetListFiles()

    }

    fun hadleGetListFiles() {
        Log.v("my", path)
        val noFilesText = findViewById<TextView>(R.id.nofiles_textview)
        val root = File(path)
        filesAndFolders = root.listFiles()

        if (filesAndFolders == null || filesAndFolders!!.size == 0) {
            noFilesText.visibility = View.VISIBLE
            return
        }

        noFilesText.visibility = View.INVISIBLE
        val adapter = ItemAdapter(filesAndFolders!!)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Log.v("my", listView.getItemAtPosition(position).toString())
            itemClick(position)
        }
        registerForContextMenu(listView)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        val info= menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedFile= filesAndFolders?.get(info.position)
        Log.v("my",selectedFile!!.name)
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(0, v.id, 0, "Rename")
        menu.add(0, v.id, 0, "Delete")
        menu.add(0, v.id, 0, "Copy")
//        val sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE)
//
//        val value = sharedPreferences.getString("key", null)
//        if(!value!!.isNullOrEmpty()){
//            menu.add(0, v.id, 0, "Paste")
//        }
    }
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info: AdapterView.AdapterContextMenuInfo = item.getMenuInfo() as AdapterView.AdapterContextMenuInfo;
        super.onContextItemSelected(item)
        val position= info.position
        val selectedFile= filesAndFolders?.get(position)

        if (item.title === "Rename") {
            val dialog= AlertDialog.Builder(this)
            dialog.setTitle("Rename to")
            val input = EditText(this)
            input.hint="New name"
            val renamePath=selectedFile!!.absolutePath
            input.setText(renamePath.substring(renamePath.lastIndexOf('/')+1))
            input.inputType=InputType.TYPE_CLASS_TEXT

            dialog.setView(input)

            dialog.setPositiveButton("Rename",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    val s= File(renamePath).parent +"/" +input.text;
                    val newFile= File(s)
                    File(renamePath).renameTo(newFile)
                    hadleGetListFiles()
                    Toast.makeText(this,"Rename successfully!", Toast.LENGTH_LONG).show()
                }
                    )

            dialog.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    dialog.cancel()
                })
            dialog.show()
            Log.v("TAG", "Rename ")
        } else if (item.title === "Delete") {
            val dialog= AlertDialog.Builder(this)
            dialog.setTitle("Delete")
            dialog.setMessage("Do you really want to delete it?")

            dialog.setPositiveButton("Delete",
                DialogInterface.OnClickListener { dialog, whichButton ->

                  selectedFile?.let { deleteFileOrFolder(it) }
                    hadleGetListFiles()
                    Toast.makeText(this,"Delete successfully!", Toast.LENGTH_LONG).show()
                }
            )

            dialog.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    dialog.cancel()
                })
            dialog.show()
            Log.v("TAG", "email")
        } else if (item.title === "Copy") {
            val dialog= AlertDialog.Builder(this)
            dialog.setTitle("Copy")
            dialog.setMessage("Do you really want to copy it?")

            dialog.setPositiveButton("Copy",
                DialogInterface.OnClickListener { dialog, whichButton ->

                    val sharedPreferences = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("key", selectedFile!!.absolutePath)
                    editor.apply()
                    Toast.makeText(this,"Copy successfully!", Toast.LENGTH_LONG).show()
                }
            )

            dialog.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    dialog.cancel()
                })
            dialog.show()

            Log.v("TAG", "Copy")
        }else if (item.title === "Paste") {
            val dialog= AlertDialog.Builder(this)
            dialog.setTitle("Paste")
            dialog.setMessage("Do you really want to paste it?")

            dialog.setPositiveButton("Paste",
                DialogInterface.OnClickListener { dialog, whichButton ->

                    val sharedPreferences = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    val value = sharedPreferences.getString("key", null)
                    editor.clear()
                    editor.apply()
                    Toast.makeText(this,"Paste successfully!", Toast.LENGTH_LONG).show()
                }
            )

            dialog.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    dialog.cancel()
                })
            dialog.show()

            Log.v("TAG", "Copy")
        }
        return true
    }
//    fun copy(src:File,dst:File){
//        try{
//            val inValue= FileInputStream(src)
//            val outValue=FileOutputStream(dst)
//
//            var len:Int;
//            var buf= byteArrayOf(1024.toByte())
//
//            while (len=inValue.read(buf) >0){
//
//            }
//
//
//
//        }catch(e:Exception){
//
//        }
//    }

    fun deleteFileOrFolder(fileOrFolder:File){
        if(fileOrFolder.isDirectory){
            if(fileOrFolder.listFiles().size==0){
                fileOrFolder.delete()
            }else{
                val files= fileOrFolder.listFiles()
                for(file in files){
                       deleteFileOrFolder(file)
                }
                if(fileOrFolder.listFiles().size==0){
                    fileOrFolder.delete()
                }
            }
        }else{
            fileOrFolder.delete()
        }
    }

    fun itemClick(position: Int) {
        val selectedFile = filesAndFolders!![position]
        if (selectedFile.isDirectory) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            val path = selectedFile.absolutePath
            intent.putExtra("path", path)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

        } else {

            //open thte file
            try {
                val intent = Intent()
                val extension= selectedFile.extension
                if(extension=="txt"){


                    val uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "com.example.filemanager.provider",
                        selectedFile
                    )
                    val viewIntent = Intent(Intent.ACTION_VIEW)
                    viewIntent.setDataAndType(uri, "text/plain")
                    viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    if (viewIntent.resolveActivity(packageManager) != null) {
                        startActivity(viewIntent)
                    }
                }else{
                    intent.action = Intent.ACTION_VIEW
                    val type = "image/*"
                    intent.setDataAndType(Uri.parse(selectedFile.absolutePath), type)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }


            } catch (e: Exception) {
                Log.v("my",e.stackTraceToString())
                Toast.makeText(
                    applicationContext,
                    "Cannot open the file",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.add_file){
            createFile()
            return true;
        }
        else if(item.itemId==R.id.add_folder){
            createNewFolder()
            return true;
        }
        else{
            return super.onOptionsItemSelected(item)
        }

    }

    fun createFile(){
        val diaglog=AlertDialog.Builder(this);
        diaglog.setTitle("New File")
        val layout = LinearLayout(this)
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(10,10,10,10)
        val fileNameText = EditText(this)
        fileNameText.hint="File name"
        val contentText = EditText(this)
        contentText.hint="Content"

        layout.addView(fileNameText)
        layout.addView(contentText)
        diaglog.setView(layout)
        diaglog.setPositiveButton("Ok",
            DialogInterface.OnClickListener { dialog, whichButton ->
                val fileName=fileNameText.text.toString()
                val content=contentText.text.toString()
                if(title.isEmpty() || content.isEmpty()){
                Toast.makeText(this,"Please fill in all field!", Toast.LENGTH_SHORT).show()
                }else{
                    try {
                        val root = File(path)
                        val gpxfile = File(root, fileName+".txt")
                        val writer = FileWriter(gpxfile)
                        writer.append(content)
                        writer.flush()
                        writer.close()
                        hadleGetListFiles()
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

            })
        diaglog.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, whichButton ->
                dialog.cancel()
            })

        diaglog.show()
    }
    fun createNewFolder(){
        val newFolderDiaglog=AlertDialog.Builder(this);
        newFolderDiaglog.setTitle("New folder")
        val  input= EditText(this);
        input.inputType=(InputType.TYPE_CLASS_TEXT)
        newFolderDiaglog.setView(input)
        newFolderDiaglog.setPositiveButton("Ok",
            DialogInterface.OnClickListener { dialog, whichButton ->
                val value = input.text.toString()
                // Do something with value!
                Log.v("my save",path+"/$value")
                //This is where you would put your make directory code


                val newFolder = File(path +"/$value")
                if(value.isEmpty() ){
                    Toast.makeText(this,"Please fill in all field!", Toast.LENGTH_SHORT).show()
                }else if(newFolder.exists()){
                    Toast.makeText(this,"Folder has exist!", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this,"Folder created!", Toast.LENGTH_SHORT).show()
                    newFolder.mkdir()
                    hadleGetListFiles()
                }


            })
        newFolderDiaglog.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, whichButton ->
                dialog.cancel()
            })

        newFolderDiaglog.show()
    }


}