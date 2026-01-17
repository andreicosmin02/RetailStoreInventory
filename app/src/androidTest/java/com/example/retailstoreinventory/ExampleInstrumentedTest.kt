package com.example.retailstoreinventory

import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.daos.ProductDao

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.retailstoreinventory", appContext.packageName)
    }
}


@RunWith(AndroidJUnit4::class)
class RoomTest {


    @Test
    fun testDatabaseCreation() {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RetailDatabase::class.java
        ).build()

        assertNotNull(db)
        db.close()
    }
}