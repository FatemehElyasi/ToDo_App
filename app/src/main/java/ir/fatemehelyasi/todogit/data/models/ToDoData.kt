package ir.fatemehelyasi.todogit.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.fatemehelyasi.todogit.data.models.Priority
import kotlinx.parcelize.Parcelize

@Entity(tableName = "ToDo_table")

@Parcelize
data class ToDoData(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    var title:String,
    var priority: Priority,
    var description:String
):Parcelable
