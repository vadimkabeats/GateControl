package com.example.gatecontrol

import android.annotation.SuppressLint
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.core.view.WindowCompat
import com.example.gatecontrol.ui.MainScreen

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    private val requestBluetoothPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val okConnect = perms[Manifest.permission.BLUETOOTH_CONNECT] == true
        val okScan    = perms[Manifest.permission.BLUETOOTH_SCAN] == true
        Log.d(TAG, "Permissions granted: CONNECT=$okConnect, SCAN=$okScan")
        checkBluetoothAndConnect()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestBluetoothPermissions.launch(arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        ))

        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetoothAndConnect() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            Log.e(TAG, "Bluetooth не поддерживается на этом устройстве")
            return
        }

        if (!adapter.isEnabled) {
            startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BT
            )
        } else {
            Log.d(TAG, "Bluetooth включён, вызываем connect()")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (bluetoothAdapter?.isEnabled == true) {
                Log.d(TAG, "Пользователь включил Bluetooth")
            } else {
                Log.e(TAG, "Пользователь отказался включать Bluetooth")
            }
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1001
    }
}
