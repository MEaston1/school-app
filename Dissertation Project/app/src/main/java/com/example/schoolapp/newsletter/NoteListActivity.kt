import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import com.example.schoolapp.R
import com.example.schoolapp.newsletter.NoteActivity


import kotlinx.android.synthetic.main.news_activity_notelist.*
import kotlinx.android.synthetic.main.news_content_notelist.*

class NoteListActivity : AppCompatActivity() {
3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_activity_notelist)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val activityIntent = Intent(this, NoteActivity::class.java)
            startActivity(activityIntent)
        }

        fab.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        listItems.layoutManager = LinearLayoutManager(this)

    }

    override fun onResume() {
        super.onResume()

    }
}