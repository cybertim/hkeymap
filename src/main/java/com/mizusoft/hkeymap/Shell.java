package com.mizusoft.hkeymap;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Tim
 */
public class Shell extends AsyncTask<String, String, Boolean> {

    private ShellExec shellExec = null;
    private String buffer;

    public void setShellExec(ShellExec shellExec) {
        this.shellExec = shellExec;
    }

    @Override
    protected Boolean doInBackground(String... paramss) {
        boolean result = true;
        this.buffer = "";
        try {
            Runtime terminal = (Runtime) Runtime.getRuntime();
            Process process = terminal.exec("su");
            DataOutputStream stdout = new DataOutputStream(process.getOutputStream());
            int count = paramss.length;
            for (int i = 0; i < count; i++) {
                Log.i(Shell.class.getName(), "*** EXEC: " + paramss[i]);
                stdout.writeBytes(paramss[i] + "\n");
            }
            stdout.flush();
            stdout.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int n;
            char[] buffer = new char[1024];
            while ((n = reader.read(buffer)) != -1) {
                publishProgress(String.valueOf(buffer, 0, n));
            }

            process.waitFor();
            if (process.exitValue() != 0) {
                result = false;
            }
            stdout.close();
        } catch (IOException ioException) {
            Log.e(Shell.class.getName(), ioException.getMessage());
            result = false;
        } catch (InterruptedException interruptedException) {
            Log.e(Shell.class.getName(), interruptedException.getMessage());
            result = false;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (shellExec != null) {
            shellExec.execute(result, buffer);
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        for (String s : values) {
            this.buffer += s;
        }
    }
}
