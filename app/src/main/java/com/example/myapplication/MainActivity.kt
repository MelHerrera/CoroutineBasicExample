package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(),CoroutineScope {
private lateinit var job: Job

   override val coroutineContext: CoroutineContext
        get() { return Dispatchers.Main + job }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job= SupervisorJob()

       btnCheck.setOnClickListener {
           //GlobalScopeCoroutine()
           //lifecycleScopeCoroutine()
           coroutineContext()
       }
    }

    private fun GlobalScopeCoroutine() {
        //Aun esta no es la forma correcta ya que GlobalScope permanece activo hasta que muere toda la aplicacion
        //x/e si la solicitud al server aun no ha traido el resultado, en ese tiempo lanzamos una nueva activity dara error cuando quiera imprimir el TOAST en este
        //contexto, la solucion seria ocupar lifeCycleScope de la libreria lifeCycle o sino un Contexto de corrutina que se destruya con la actividad
        GlobalScope.launch(Dispatchers.Main) {
            val succes= withContext(Dispatchers.IO){googleCheck()}
            toast(if(succes) "Succes request" else "Error request")
        }
    }

    private fun lifecycleScopeCoroutine(){
        //1. primero agregar la libreria androidx.lifecycle:lifecycle-runtime-ktx:2.2.0
        lifecycleScope.launch {
            val succes= withContext(Dispatchers.IO){googleCheck()}
            toast(if(succes) "Succes request" else "Error request")
        }
    }

    private fun coroutineContext(){
        //utilizando un contexto de coroutine
        //1. Implementar la interfaz CoroutineScope
        //2.Sobreescribir el miembro Get() definiendo el scope y un job manejador: override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
        //2.1 inicializar el job ;  job= SupervisorJob()
        //3. put in this method the server request
        //4. Cuando la actividad sea destruida cancelar todos los jobs para ello sobreescribir el onDestroy()
        launch {
            val succes= withContext(Dispatchers.IO){googleCheck()}
            toast(if(succes) "Succes request" else "Error request")
        }
    }

    private fun toast(mssge:String){
        Toast.makeText(this,mssge,Toast.LENGTH_SHORT).show()
    }

    private fun googleCheck():Boolean{
        return try {
            val mUrl=URL("https://www.google.com")
            val httpReques = mUrl.openConnection() as HttpURLConnection
            httpReques.connectTimeout=3000
            httpReques.connect()
            httpReques.responseCode==HttpURLConnection.HTTP_OK
        }catch (e:Exception){
            Log.d("ErrorMessage",e.message.toString())
            false
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}