//package com.example.tripplan
//
//import android.content.ContentValues
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//
//class DBHelper(context: Context) :
//    SQLiteOpenHelper(context, "Login.db", null, 1) {
//    // users 테이블 생성
//    override fun onCreate(MyDB: SQLiteDatabase?) {
//        MyDB!!.execSQL("create Table users(id TEXT primary key, password TEXT, nick TEXT, phone TEXT)")
//    }
//
//    // 정보 갱신
//    override fun onUpgrade(MyDB: SQLiteDatabase?, i: Int, i1: Int) {
//        MyDB!!.execSQL("drop Table if exists users")
//    }
//
//    // id, password, nick, phone 삽입 (성공시 true, 실패시 false)
//    fun insertData (id: String?, password: String?, nick: String?, phone: String?): Boolean {
//        val MyDB = this.writableDatabase
//        val contentValues = ContentValues()
//        contentValues.put("id", id)
//        contentValues.put("password", password)
//        contentValues.put("nick", nick)
//        contentValues.put("phone", phone)
//        val result = MyDB.insert("users", null, contentValues)
//        MyDB.close()
//        return if (result == -1L) false else true
//    }
//
//    // 사용자 아이디가 없으면 false, 이미 존재하면 true
//    fun checkUser(id: String?): Boolean {
//        val MyDB = this.readableDatabase
//        var res = true
//        val cursor = MyDB.rawQuery("Select * from users where id =?", arrayOf(id))
//        if (cursor.count <= 0) res = false
//        return res
//    }
//
//    // 사용자 닉네임이 없으면 false, 이미 존재하면 true
//    fun checkNick(nick: String?): Boolean {
//        val MyDB = this.readableDatabase
//        var res = true
//        val cursor = MyDB.rawQuery("Select * from users where nick =?", arrayOf(nick))
//        if (cursor.count <= 0) res = false
//        return res
//    }
//
//    // 해당 id, password가 있는지 확인 (없다면 false)
//    fun checkUserpass(id: String, password: String) : Boolean {
//        val MyDB = this.writableDatabase
//        var res = true
//        val cursor = MyDB.rawQuery(
//            "Select * from users where id = ? and password = ?",
//            arrayOf(id, password)
//        )
//        if (cursor.count <= 0) res = false
//        return res
//    }
//
//    // DB name을 Login.db로 설정
//    companion object {
//        const val DBNAME = "Login.db"
//    }
//
//    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
//
//    // 사용자 정보를 Firebase Realtime Database에 추가
//    fun addUser(user: User) {
//        database.child("user").child(user.uId).setValue(user)
//    }
//
//    // 특정 사용자 정보를 가져오기
//    fun getUser(userId: String, callback: (User?) -> Unit) {
//        database.child("user").child(userId).get().addOnSuccessListener { snapshot ->
//            val user = snapshot.getValue(User::class.java)
//            callback(user)
//        }.addOnFailureListener {
//            callback(null)
//        }
//    }
//
//    // 사용자 권한 확인
//    fun isUserAuthorized(userId: String, callback: (Boolean) -> Unit) {
//        database.child("user").child(userId).child("authorized").get().addOnSuccessListener { snapshot ->
//            callback(snapshot.getValue(Boolean::class.java) == true)
//        }.addOnFailureListener {
//            callback(false)
//        }
//    }
//}