/*
 * Copyright (C) 2017 Jakob Nixdorf
 * Copyright (C) 2015 Bruno Bierbaumer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;

import java.io.File;
import java.util.ArrayList;

import javax.crypto.SecretKey;

public class DatabaseHelper {
    public static final String KEY_FILE = "otp.key";
    public static final String SETTINGS_FILE = "secrets.dat";

    /* Database functions */

    public static void saveDatabase(Context context, ArrayList<Entry> entries) {
        String jsonString = entriesToString(entries);

        try {
            byte[] data = jsonString.getBytes();

            SecretKey key = KeyStoreHelper.loadOrGenerateWrappedKey(context, new File(context.getFilesDir() + "/" + KEY_FILE));
            data = EncryptionHelper.encrypt(key, data);

            FileHelper.writeBytesToFile(new File(context.getFilesDir() + "/" + SETTINGS_FILE), data);

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static ArrayList<Entry> loadDatabase(Context context){
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            byte[] data = FileHelper.readFileToBytes(new File(context.getFilesDir() + "/" + SETTINGS_FILE));

            SecretKey key = KeyStoreHelper.loadOrGenerateWrappedKey(context, new File(context.getFilesDir() + "/" + KEY_FILE));
            data = EncryptionHelper.decrypt(key, data);

            entries = stringToEntries(context, new String(data));
        } catch (Exception error) {
            error.printStackTrace();
        }

        return entries;
    }

    /* Conversion functions */

    public static String entriesToString(ArrayList<Entry> entries) {
        JSONArray json = new JSONArray();

        for(Entry e: entries){
            try {
                json.put(e.toJSON());
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        return json.toString();
    }

    public static ArrayList<Entry> stringToEntries(Context context, String data) {
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            JSONArray json = new JSONArray(data);

            for (int i = 0; i < json.length(); i++) {
                Entry entry = new Entry(json.getJSONObject(i));

                int size = context.getResources().getDimensionPixelSize(R.dimen.card_thumbnail_size);
                entry.setThumbnailImage(EntryThumbnail.getThumbnailGraphic(context, entry.getLabel(), size, entry.getThumbnail()));

                entries.add(entry);
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return entries;
    }

    /* Export functions */

    public static boolean exportAsJSON(Context context, Uri file) {
        ArrayList<Entry> entries = loadDatabase(context);

        return FileHelper.writeStringToFile(context, file, entriesToString(entries));
    }

    public static ArrayList<Entry> importFromJSON(Context context, Uri file) {
        String content = FileHelper.readFileToString(context, file);

        ArrayList<Entry> entries = null;

        if (! content.isEmpty())
            entries = stringToEntries(context, content);

        return entries;
    }
}