package cat.carlinapalius.data

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Mira a GitHub si hi ha una versió nova i, si n'hi ha, la baixa i la instal·la.
 *
 * La primera actualització demana confirmació (l'app l'havia instal·lat el navegador).
 * A partir d'aleshores l'app és qui s'instal·la a si mateixa i, a Android 12 o més,
 * les actualitzacions s'instal·len soles sense preguntar.
 */
object Updater {
    private const val TAG = "Updater"
    private const val BASE = "https://github.com/realbluehead/carlina-palius/releases/latest/download"
    private const val CHECK_INTERVAL_MS = 30 * 60 * 1000L

    private var lastCheck = 0L

    suspend fun checkAndInstall(context: Context) {
        val now = System.currentTimeMillis()
        if (now - lastCheck < CHECK_INTERVAL_MS) return
        lastCheck = now
        try {
            val latest = withContext(Dispatchers.IO) { fetchLatest() }
            val installed = installedVersionCode(context)
            if (latest.code <= installed) return
            Log.i(TAG, "Versió nova ${latest.name} (${latest.code} > $installed)")
            Toast.makeText(context, "Actualitzant a la versió ${latest.name}…", Toast.LENGTH_SHORT).show()
            withContext(Dispatchers.IO) { downloadAndInstall(context.applicationContext) }
        } catch (e: Exception) {
            // Sense xarxa o sense release: ja ho tornarem a provar més tard.
            Log.w(TAG, "No s'ha pogut comprovar l'actualització", e)
            lastCheck = 0L
        }
    }

    private class Latest(val code: Long, val name: String)

    private fun fetchLatest(): Latest {
        val json = JSONObject(open("$BASE/version.json").inputStream.bufferedReader().use { it.readText() })
        return Latest(json.getLong("versionCode"), json.getString("versionName"))
    }

    private fun installedVersionCode(context: Context): Long =
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode

    private fun downloadAndInstall(context: Context) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
        }
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            open("$BASE/carlina-palius.apk").inputStream.use { input ->
                session.openWrite("carlina-palius.apk", 0, -1).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }
            val callback = PendingIntent.getBroadcast(
                context,
                sessionId,
                Intent(context, InstallResultReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
            session.commit(callback.intentSender)
        }
    }

    private fun open(url: String): HttpURLConnection {
        // GitHub redirigeix les descàrregues a un altre domini; HttpURLConnection ho segueix (https → https).
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 60_000
        connection.useCaches = false
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw IllegalStateException("HTTP ${connection.responseCode} per $url")
        }
        return connection
    }
}

/** Rep el resultat de la instal·lació; si Android vol confirmació, li mostra el diàleg. */
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                @Suppress("DEPRECATION")
                val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT) ?: return
                context.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            PackageInstaller.STATUS_SUCCESS -> Log.i("Updater", "Actualització instal·lada")
            else -> Log.w(
                "Updater",
                "La instal·lació ha fallat: ${intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)}",
            )
        }
    }
}
