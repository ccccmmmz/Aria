package com.arialyy.simple.refactor

import android.content.Context
import androidx.room.*

/**
 * room工具类
 */
object RoomKit {

    fun testBook(context: Context) {
        val db = Room.databaseBuilder(context, SQLDatabase::class.java, "bookDb").build()
        CoroutinueKit.executeIoTask {
            db.book().insert(Book(100, "100的书"))
//            println("1with thread ${Thread.currentThread().name}")
        }
        println("book insert success")

        CoroutinueKit.executeIoTask {
            val qeuryAll = db.book().qeuryAll()
            println("query books  = ${qeuryAll.size} with thread ${Thread.currentThread().name}")
        }

    }
}

@Database(entities = [Book::class], version = 1)
abstract class SQLDatabase : RoomDatabase() {
    abstract fun book(): BookDao
}

@Entity
data class Book(
    @PrimaryKey(autoGenerate = true)
    var number: Long = 0,
    var title: String
)

@Dao
interface BookDao {
    @Query("select * from Book")
    fun qeuryAll(): List<Book>

    @Insert
    fun insert(vararg book: Book): List<Long>

    @Delete
    fun delete(book: Book): Int

    @Update
    fun update(book: Book): Int

}

