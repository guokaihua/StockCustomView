package com.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPastDate(-200)
    }

    /**
     * 获取过去第几天的日期(- 操作) 或者 未来 第几天的日期( + 操作)
     *
     * @param past
     * @return
     */
    fun getPastDate(past: Int): String {
        val calendar = Calendar.getInstance()
        val date = Date(118, 6, 3)
        calendar.setTime(date)
        var m = 1
        if (past < 0)
            m = -1
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + m)
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + past)
        val today = calendar.getTime()
        val format = SimpleDateFormat("yyyy-MM-dd")
        val result = format.format(today)
        return result
    }
}
