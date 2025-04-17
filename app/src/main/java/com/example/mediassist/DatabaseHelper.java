package com.example.mediassist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MediAssist.db";
    private static final int DATABASE_VERSION = 6;

    // Table Utilisateurs
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Table Medications
    private static final String TABLE_MEDICATIONS = "medications";
    private static final String COLUMN_MED_ID = "med_id";
    private static final String COLUMN_MED_NAME = "name";
    private static final String COLUMN_MED_TYPE = "type";
    private static final String COLUMN_MED_FREQUENCY = "frequency";
    private static final String COLUMN_MED_DOSAGE = "dosage";
    private static final String COLUMN_MED_TIME = "time";
    private static final String COLUMN_MED_IMAGE_PATH = "image_path";
    private static final String COLUMN_MED_USER_ID = "user_id";

    // Table Contacts d'urgence
    private static final String TABLE_CONTACTS = "emergency_contacts";
    private static final String COLUMN_CONTACT_ID = "contact_id";
    private static final String COLUMN_CONTACT_NAME = "name";
    private static final String COLUMN_CONTACT_PHONE = "phone";
    private static final String COLUMN_CONTACT_RELATION = "relation";
    private static final String COLUMN_CONTACT_IMAGE_PATH = "image_path";
    private static final String COLUMN_CONTACT_USER_ID = "user_id";

    // Table Ordonnances
    private static final String TABLE_PRESCRIPTIONS = "prescriptions";
    private static final String COLUMN_PRESCRIPTION_ID = "prescription_id";
    private static final String COLUMN_PRESCRIPTION_TITLE = "title";
    private static final String COLUMN_PRESCRIPTION_IMAGE_PATH = "image_path";
    private static final String COLUMN_PRESCRIPTION_USER_ID = "user_id";

    // table des rendez-vous
    private static final String TABLE_APPOINTMENTS = "appointments";
    private static final String COLUMN_APPOINTMENT_ID = "appointment_id";
    private static final String COLUMN_APPOINTMENT_TITLE = "title";
    private static final String COLUMN_APPOINTMENT_DETAILS = "details";
    private static final String COLUMN_APPOINTMENT_DATE = "date";
    private static final String COLUMN_APPOINTMENT_LOCATION = "location";
    private static final String COLUMN_APPOINTMENT_CATEGORY = "category";
    private static final String COLUMN_APPOINTMENT_NOTES = "notes";
    private static final String COLUMN_APPOINTMENT_USER_ID = "user_id";

    // Requête de création de table utilisateurs
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EMAIL + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT" + ");";

    // Requête de création de table médicaments
    private static final String CREATE_MEDICATIONS_TABLE =
            "CREATE TABLE " + TABLE_MEDICATIONS + " (" +
                    COLUMN_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MED_NAME + " TEXT, " +
                    COLUMN_MED_TYPE + " TEXT, " +
                    COLUMN_MED_FREQUENCY + " TEXT, " +
                    COLUMN_MED_DOSAGE + " TEXT, " +
                    COLUMN_MED_TIME + " TEXT, " +
                    COLUMN_MED_IMAGE_PATH + " TEXT, " +
                    COLUMN_MED_USER_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_MED_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_ID + ")" + ");";

    // Requête de création de table contacts d'urgence
    private static final String CREATE_CONTACTS_TABLE =
            "CREATE TABLE " + TABLE_CONTACTS + " (" +
                    COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CONTACT_NAME + " TEXT, " +
                    COLUMN_CONTACT_PHONE + " TEXT, " +
                    COLUMN_CONTACT_RELATION + " TEXT, " +
                    COLUMN_CONTACT_IMAGE_PATH + " TEXT, " +
                    COLUMN_CONTACT_USER_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_CONTACT_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_ID + ")" + ");";

    // Requête de création de table ordonnances
    private static final String CREATE_PRESCRIPTIONS_TABLE =
            "CREATE TABLE " + TABLE_PRESCRIPTIONS + " (" +
                    COLUMN_PRESCRIPTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRESCRIPTION_TITLE + " TEXT, " +
                    COLUMN_PRESCRIPTION_IMAGE_PATH + " TEXT, " +
                    COLUMN_PRESCRIPTION_USER_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_PRESCRIPTION_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_ID + ")" + ");";

    //requête de création de table rendez-vous
    private static final String CREATE_APPOINTMENTS_TABLE =
            "CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
                    COLUMN_APPOINTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_APPOINTMENT_TITLE + " TEXT, " +
                    COLUMN_APPOINTMENT_DETAILS + " TEXT, " +
                    COLUMN_APPOINTMENT_DATE + " TEXT, " +
                    COLUMN_APPOINTMENT_LOCATION + " TEXT, " +
                    COLUMN_APPOINTMENT_CATEGORY + " TEXT, " +
                    COLUMN_APPOINTMENT_NOTES + " TEXT, " +
                    COLUMN_APPOINTMENT_USER_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_APPOINTMENT_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_ID + ")" + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_MEDICATIONS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_PRESCRIPTIONS_TABLE);
        db.execSQL(CREATE_APPOINTMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            // Ajouter la table des rendez-vous si elle n'existe pas
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
            db.execSQL(CREATE_APPOINTMENTS_TABLE);
        }

        // Laissez le reste du code onUpgrade inchangé
        if (oldVersion < 4) {
            // Ajouter la table des ordonnances si elle n'existe pas
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESCRIPTIONS);
            db.execSQL(CREATE_PRESCRIPTIONS_TABLE);
        }
        if (oldVersion < 4) {
            // Ajouter la table des ordonnances si elle n'existe pas
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESCRIPTIONS);
            db.execSQL(CREATE_PRESCRIPTIONS_TABLE);
        }

        if (oldVersion < 3) {
            // Ajouter la table des contacts d'urgence si elle n'existe pas
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
            db.execSQL(CREATE_CONTACTS_TABLE);
        }

        if (oldVersion < 2) {
            // Mise à jour de la version 1 à 2
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
            db.execSQL(CREATE_MEDICATIONS_TABLE);
        }
    }

    // Méthodes pour les utilisateurs
    public boolean insertUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1; // Retourne true si l'insertion est réussie
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public long getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS + " WHERE " +
                COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        long userId = -1;
        if (cursor.moveToFirst()) {
            try {
                userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        db.close();
        return userId;
    }

    // Méthodes pour les médicaments
    public long insertMedication(String name, String type, String frequency, String dosage,
                                 String time, String imagePath, long userId) {
        Log.d("DatabaseHelper", "Inserting medication: " + name + " for user: " + userId);

        SQLiteDatabase db = null;
        long id = -1;

        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_MED_NAME, name);
            values.put(COLUMN_MED_TYPE, type);
            values.put(COLUMN_MED_FREQUENCY, frequency);
            values.put(COLUMN_MED_DOSAGE, dosage);
            values.put(COLUMN_MED_TIME, time);
            values.put(COLUMN_MED_IMAGE_PATH, imagePath);
            values.put(COLUMN_MED_USER_ID, userId);

            // Vérifier si la table existe
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_MEDICATIONS + "'", null);
            boolean tableExists = cursor != null && cursor.getCount() > 0;
            if (cursor != null) {
                cursor.close();
            }

            if (!tableExists) {
                Log.e("DatabaseHelper", "Table " + TABLE_MEDICATIONS + " does not exist!");
                db.execSQL(CREATE_MEDICATIONS_TABLE);
                Log.d("DatabaseHelper", "Created medications table");
            }

            // Insérer les données
            id = db.insert(TABLE_MEDICATIONS, null, values);
            Log.d("DatabaseHelper", "Insert result: " + id);

            // Vérifier si l'insertion a réussi
            if (id == -1) {
                Log.e("DatabaseHelper", "Failed to insert medication");
            } else {
                Log.d("DatabaseHelper", "Medication inserted with ID: " + id);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting medication", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return id;
    }

    public boolean updateMedication(long medId, String name, String type, String frequency,
                                    String dosage, String time, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MED_NAME, name);
        values.put(COLUMN_MED_TYPE, type);
        values.put(COLUMN_MED_FREQUENCY, frequency);
        values.put(COLUMN_MED_DOSAGE, dosage);
        values.put(COLUMN_MED_TIME, time);

        // Ne mettre à jour l'image que si une nouvelle est fournie
        if (imagePath != null && !imagePath.isEmpty()) {
            values.put(COLUMN_MED_IMAGE_PATH, imagePath);
        }

        int rowsAffected = db.update(TABLE_MEDICATIONS, values,
                COLUMN_MED_ID + " = ?",
                new String[]{String.valueOf(medId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteMedication(long medId) {
        Log.d("DatabaseHelper", "Deleting medication with ID: " + medId);
        SQLiteDatabase db = null;
        boolean success = false;

        try {
            db = this.getWritableDatabase();

            // Récupérer le chemin de l'image avant de supprimer
            String imagePath = null;
            Cursor cursor = db.query(TABLE_MEDICATIONS,
                    new String[]{COLUMN_MED_IMAGE_PATH},
                    COLUMN_MED_ID + " = ?",
                    new String[]{String.valueOf(medId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_IMAGE_PATH));
                cursor.close();
            }

            // Supprimer l'enregistrement de la base de données
            int rowsAffected = db.delete(TABLE_MEDICATIONS,
                    COLUMN_MED_ID + " = ?",
                    new String[]{String.valueOf(medId)});

            success = rowsAffected > 0;

            // Si la suppression a réussi et qu'il y a une image, supprimer le fichier
            if (success && imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    boolean fileDeleted = imageFile.delete();
                    Log.d("DatabaseHelper", "Image file deleted: " + fileDeleted);
                }
            }

            Log.d("DatabaseHelper", "Delete result: " + success);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting medication", e);
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return success;
    }

    public List<Medication> getAllMedications(long userId) {
        List<Medication> medicationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_MED_USER_ID + " = ?" +
                " ORDER BY " + COLUMN_MED_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                try {
                    Medication medication = new Medication();
                    medication.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MED_ID)));
                    medication.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_NAME)));
                    medication.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_TYPE)));
                    medication.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_FREQUENCY)));
                    medication.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_DOSAGE)));
                    medication.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_TIME)));
                    medication.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_IMAGE_PATH)));
                    medication.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MED_USER_ID)));

                    medicationList.add(medication);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return medicationList;
    }

    public List<Medication> searchMedications(String keyword, long userId) {
        List<Medication> medicationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_MED_USER_ID + " = ?" +
                " AND " + COLUMN_MED_NAME + " LIKE ?" +
                " ORDER BY " + COLUMN_MED_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), "%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                try {
                    Medication medication = new Medication();
                    medication.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MED_ID)));
                    medication.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_NAME)));
                    medication.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_TYPE)));
                    medication.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_FREQUENCY)));
                    medication.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_DOSAGE)));
                    medication.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_TIME)));
                    medication.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_IMAGE_PATH)));
                    medication.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MED_USER_ID)));

                    medicationList.add(medication);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return medicationList;
    }

    public Medication getMedicationById(long medId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_MED_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(medId)});

        Medication medication = null;
        if (cursor.moveToFirst()) {
            try {
                medication = new Medication();
                medication.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MED_ID)));
                medication.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_NAME)));
                medication.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_TYPE)));
                medication.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_FREQUENCY)));
                medication.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_DOSAGE)));
                medication.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_TIME)));
                medication.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_IMAGE_PATH)));
                medication.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MED_USER_ID)));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        db.close();
        return medication;
    }

    // Méthodes pour les contacts d'urgence
    public long insertContact(String name, String phone, String relation, String imagePath, long userId) {
        Log.d("DatabaseHelper", "Inserting contact: " + name + " for user: " + userId);

        SQLiteDatabase db = null;
        long id = -1;

        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_CONTACT_NAME, name);
            values.put(COLUMN_CONTACT_PHONE, phone);
            values.put(COLUMN_CONTACT_RELATION, relation);
            values.put(COLUMN_CONTACT_IMAGE_PATH, imagePath);
            values.put(COLUMN_CONTACT_USER_ID, userId);

            // Vérifier si la table existe
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_CONTACTS + "'", null);
            boolean tableExists = cursor != null && cursor.getCount() > 0;
            if (cursor != null) {
                cursor.close();
            }

            if (!tableExists) {
                Log.e("DatabaseHelper", "Table " + TABLE_CONTACTS + " does not exist!");
                db.execSQL(CREATE_CONTACTS_TABLE);
                Log.d("DatabaseHelper", "Created contacts table");
            }

            // Insérer les données
            id = db.insert(TABLE_CONTACTS, null, values);
            Log.d("DatabaseHelper", "Insert result: " + id);

            // Vérifier si l'insertion a réussi
            if (id == -1) {
                Log.e("DatabaseHelper", "Failed to insert contact");
            } else {
                Log.d("DatabaseHelper", "Contact inserted with ID: " + id);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting contact", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return id;
    }

    public boolean updateContact(long contactId, String name, String phone, String relation, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTACT_NAME, name);
        values.put(COLUMN_CONTACT_PHONE, phone);
        values.put(COLUMN_CONTACT_RELATION, relation);

        // Ne mettre à jour l'image que si une nouvelle est fournie
        if (imagePath != null && !imagePath.isEmpty()) {
            values.put(COLUMN_CONTACT_IMAGE_PATH, imagePath);
        }

        int rowsAffected = db.update(TABLE_CONTACTS, values,
                COLUMN_CONTACT_ID + " = ?",
                new String[]{String.valueOf(contactId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteContact(long contactId) {
        Log.d("DatabaseHelper", "Deleting contact with ID: " + contactId);
        SQLiteDatabase db = null;
        boolean success = false;

        try {
            db = this.getWritableDatabase();

            // Récupérer le chemin de l'image avant de supprimer
            String imagePath = null;
            Cursor cursor = db.query(TABLE_CONTACTS,
                    new String[]{COLUMN_CONTACT_IMAGE_PATH},
                    COLUMN_CONTACT_ID + " = ?",
                    new String[]{String.valueOf(contactId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_IMAGE_PATH));
                cursor.close();
            }

            // Supprimer l'enregistrement de la base de données
            int rowsAffected = db.delete(TABLE_CONTACTS,
                    COLUMN_CONTACT_ID + " = ?",
                    new String[]{String.valueOf(contactId)});

            success = rowsAffected > 0;

            // Si la suppression a réussi et qu'il y a une image, supprimer le fichier
            if (success && imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    boolean fileDeleted = imageFile.delete();
                    Log.d("DatabaseHelper", "Image file deleted: " + fileDeleted);
                }
            }

            Log.d("DatabaseHelper", "Delete result: " + success);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting contact", e);
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return success;
    }

    public List<Contact> getAllContacts(long userId) {
        List<Contact> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_CONTACTS +
                " WHERE " + COLUMN_CONTACT_USER_ID + " = ?" +
                " ORDER BY " + COLUMN_CONTACT_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                try {
                    Contact contact = new Contact();
                    contact.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_ID)));
                    contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_NAME)));
                    contact.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_PHONE)));
                    contact.setRelation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_RELATION)));
                    contact.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_IMAGE_PATH)));
                    contact.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_USER_ID)));

                    contactList.add(contact);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contactList;
    }

    public List<Contact> searchContacts(String keyword, long userId) {
        List<Contact> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_CONTACTS +
                " WHERE " + COLUMN_CONTACT_USER_ID + " = ?" +
                " AND " + COLUMN_CONTACT_NAME + " LIKE ?" +
                " ORDER BY " + COLUMN_CONTACT_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), "%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                try {
                    Contact contact = new Contact();
                    contact.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_ID)));
                    contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_NAME)));
                    contact.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_PHONE)));
                    contact.setRelation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_RELATION)));
                    contact.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_IMAGE_PATH)));
                    contact.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_USER_ID)));

                    contactList.add(contact);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contactList;
    }

    public Contact getContactById(long contactId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_CONTACTS +
                " WHERE " + COLUMN_CONTACT_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(contactId)});

        Contact contact = null;
        if (cursor.moveToFirst()) {
            try {
                contact = new Contact();
                contact.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_NAME)));
                contact.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_PHONE)));
                contact.setRelation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_RELATION)));
                contact.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_IMAGE_PATH)));
                contact.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_USER_ID)));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        db.close();
        return contact;
    }

    // Méthodes pour les ordonnances
    public long insertPrescription(String title, String imagePath, long userId) {
        Log.d("DatabaseHelper", "Inserting prescription: " + title + " for user: " + userId);

        SQLiteDatabase db = null;
        long id = -1;

        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_PRESCRIPTION_TITLE, title);
            values.put(COLUMN_PRESCRIPTION_IMAGE_PATH, imagePath);
            values.put(COLUMN_PRESCRIPTION_USER_ID, userId);

            // Vérifier si la table existe
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_PRESCRIPTIONS + "'", null);
            boolean tableExists = cursor != null && cursor.getCount() > 0;
            if (cursor != null) {
                cursor.close();
            }

            if (!tableExists) {
                Log.e("DatabaseHelper", "Table " + TABLE_PRESCRIPTIONS + " does not exist!");
                db.execSQL(CREATE_PRESCRIPTIONS_TABLE);
                Log.d("DatabaseHelper", "Created prescriptions table");
            }

            // Insérer les données
            id = db.insert(TABLE_PRESCRIPTIONS, null, values);
            Log.d("DatabaseHelper", "Insert result: " + id);

            // Vérifier si l'insertion a réussi
            if (id == -1) {
                Log.e("DatabaseHelper", "Failed to insert prescription");
            } else {
                Log.d("DatabaseHelper", "Prescription inserted with ID: " + id);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting prescription", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return id;
    }

    public boolean deletePrescription(long prescriptionId) {
        Log.d("DatabaseHelper", "Deleting prescription with ID: " + prescriptionId);
        SQLiteDatabase db = null;
        boolean success = false;

        try {
            db = this.getWritableDatabase();

            // Récupérer le chemin de l'image avant de supprimer
            String imagePath = null;
            Cursor cursor = db.query(TABLE_PRESCRIPTIONS,
                    new String[]{COLUMN_PRESCRIPTION_IMAGE_PATH},
                    COLUMN_PRESCRIPTION_ID + " = ?",
                    new String[]{String.valueOf(prescriptionId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRESCRIPTION_IMAGE_PATH));
                cursor.close();
            }

            // Supprimer l'enregistrement de la base de données
            int rowsAffected = db.delete(TABLE_PRESCRIPTIONS,
                    COLUMN_PRESCRIPTION_ID + " = ?",
                    new String[]{String.valueOf(prescriptionId)});

            success = rowsAffected > 0;

            // Si la suppression a réussi et qu'il y a une image, supprimer le fichier
            if (success && imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    boolean fileDeleted = imageFile.delete();
                    Log.d("DatabaseHelper", "Image file deleted: " + fileDeleted);
                }
            }

            Log.d("DatabaseHelper", "Delete result: " + success);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting prescription", e);
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return success;
    }

    public List<Prescription> getAllPrescriptions(long userId) {
        List<Prescription> prescriptionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PRESCRIPTIONS +
                " WHERE " + COLUMN_PRESCRIPTION_USER_ID + " = ?" +
                " ORDER BY " + COLUMN_PRESCRIPTION_ID + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                try {
                    Prescription prescription = new Prescription();
                    prescription.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PRESCRIPTION_ID)));
                    prescription.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRESCRIPTION_TITLE)));
                    prescription.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRESCRIPTION_IMAGE_PATH)));
                    prescription.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PRESCRIPTION_USER_ID)));

                    prescriptionList.add(prescription);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return prescriptionList;
    }

    public Prescription getPrescriptionById(long prescriptionId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PRESCRIPTIONS +
                " WHERE " + COLUMN_PRESCRIPTION_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(prescriptionId)});

        Prescription prescription = null;
        if (cursor.moveToFirst()) {
            try {
                prescription = new Prescription();
                prescription.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PRESCRIPTION_ID)));
                prescription.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRESCRIPTION_TITLE)));
                prescription.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRESCRIPTION_IMAGE_PATH)));
                prescription.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PRESCRIPTION_USER_ID)));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        db.close();
        return prescription;
    }

    // Méthode pour vérifier la structure de la base de données
    public void checkDatabaseStructure() {
        SQLiteDatabase db = null;
        try {
            db = this.getReadableDatabase();

            // Vérifier si les tables existent
            String[] tables = {TABLE_MEDICATIONS, TABLE_CONTACTS, TABLE_PRESCRIPTIONS};
            for (String table : tables) {
                Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'", null);
                boolean tableExists = cursor != null && cursor.getCount() > 0;
                Log.d("DatabaseHelper", "Table " + table + " exists: " + tableExists);

                if (cursor != null) {
                    cursor.close();
                }

                // Si la table existe, vérifier sa structure
                if (tableExists) {
                    cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
                    if (cursor != null) {
                        Log.d("DatabaseHelper", "Table structure for " + table + ":");
                        while (cursor.moveToNext()) {
                            String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                            String columnType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                            Log.d("DatabaseHelper", columnName + " - " + columnType);
                        }
                        cursor.close();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking database structure", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Méthodes pour les rendez-vous
    public long insertAppointment(String title, String details, String date, String location,
                                  String category, String notes, long userId) {
        Log.d("DatabaseHelper", "Inserting appointment: " + title + " for user: " + userId);

        SQLiteDatabase db = null;
        long id = -1;

        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_APPOINTMENT_TITLE, title);
            values.put(COLUMN_APPOINTMENT_DETAILS, details);
            values.put(COLUMN_APPOINTMENT_DATE, date);
            values.put(COLUMN_APPOINTMENT_LOCATION, location);
            values.put(COLUMN_APPOINTMENT_CATEGORY, category);
            values.put(COLUMN_APPOINTMENT_NOTES, notes);
            values.put(COLUMN_APPOINTMENT_USER_ID, userId);

            // Vérifier si la table existe
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_APPOINTMENTS + "'", null);
            boolean tableExists = cursor != null && cursor.getCount() > 0;
            if (cursor != null) {
                cursor.close();
            }

            if (!tableExists) {
                Log.e("DatabaseHelper", "Table " + TABLE_APPOINTMENTS + " does not exist!");
                db.execSQL(CREATE_APPOINTMENTS_TABLE);
                Log.d("DatabaseHelper", "Created appointments table");
            }

            // Insérer les données
            id = db.insert(TABLE_APPOINTMENTS, null, values);
            Log.d("DatabaseHelper", "Insert result: " + id);

            // Vérifier si l'insertion a réussi
            if (id == -1) {
                Log.e("DatabaseHelper", "Failed to insert appointment");
            } else {
                Log.d("DatabaseHelper", "Appointment inserted with ID: " + id);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting appointment", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return id;
    }

    public boolean updateAppointment(long appointmentId, String title, String details, String date,
                                     String location, String category, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APPOINTMENT_TITLE, title);
        values.put(COLUMN_APPOINTMENT_DETAILS, details);
        values.put(COLUMN_APPOINTMENT_DATE, date);
        values.put(COLUMN_APPOINTMENT_LOCATION, location);
        values.put(COLUMN_APPOINTMENT_CATEGORY, category);
        values.put(COLUMN_APPOINTMENT_NOTES, notes);

        int rowsAffected = db.update(TABLE_APPOINTMENTS, values,
                COLUMN_APPOINTMENT_ID + " = ?",
                new String[]{String.valueOf(appointmentId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteAppointment(long appointmentId) {
        Log.d("DatabaseHelper", "Deleting appointment with ID: " + appointmentId);
        SQLiteDatabase db = null;
        boolean success = false;

        try {
            db = this.getWritableDatabase();

            // Supprimer l'enregistrement de la base de données
            int rowsAffected = db.delete(TABLE_APPOINTMENTS,
                    COLUMN_APPOINTMENT_ID + " = ?",
                    new String[]{String.valueOf(appointmentId)});

            success = rowsAffected > 0;
            Log.d("DatabaseHelper", "Delete result: " + success);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting appointment", e);
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return success;
    }

    public List<Appointment> getAllAppointments(long userId) {
        List<Appointment> appointmentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COLUMN_APPOINTMENT_USER_ID + " = ?" +
                " ORDER BY " + COLUMN_APPOINTMENT_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                try {
                    Appointment appointment = new Appointment();
                    appointment.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_ID)));
                    appointment.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_TITLE)));
                    appointment.setDetails(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_DETAILS)));
                    appointment.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_DATE)));
                    appointment.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_LOCATION)));
                    appointment.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_CATEGORY)));
                    appointment.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_NOTES)));
                    appointment.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_USER_ID)));

                    appointmentList.add(appointment);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return appointmentList;
    }

    public Appointment getAppointmentById(long appointmentId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COLUMN_APPOINTMENT_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(appointmentId)});

        Appointment appointment = null;
        if (cursor.moveToFirst()) {
            try {
                appointment = new Appointment();
                appointment.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_ID)));
                appointment.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_TITLE)));
                appointment.setDetails(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_DETAILS)));
                appointment.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_DATE)));
                appointment.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_LOCATION)));
                appointment.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_CATEGORY)));
                appointment.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_NOTES)));
                appointment.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_USER_ID)));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        db.close();
        return appointment;
    }

    // Ajoutez ces méthodes à votre classe DatabaseHelper existante

    // Méthode pour récupérer tous les événements (médicaments et rendez-vous) pour une date spécifique
    public List<ScheduleEvent> getEventsForDate(String date, long userId) {
        List<ScheduleEvent> events = new ArrayList<>();

        // Ajouter les médicaments pour cette date
        events.addAll(getMedicationsForDate(date, userId));

        // Ajouter les rendez-vous pour cette date
        events.addAll(getAppointmentsForDate(date, userId));

        return events;
    }

    // Méthode pour récupérer les médicaments pour une date spécifique
    private List<ScheduleEvent> getMedicationsForDate(String date, long userId) {
        List<ScheduleEvent> medicationEvents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Nous supposons que la date est stockée dans le format "yyyy-MM-dd"
        // et que le temps est stocké séparément
        String query = "SELECT * FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_MED_USER_ID + " = ?" +
                " ORDER BY " + COLUMN_MED_TIME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                try {
                    // Pour simplifier, nous supposons que tous les médicaments sont pris tous les jours
                    // Dans une implémentation réelle, vous devriez vérifier la fréquence
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_NAME));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_TIME));
                    String dosage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MED_DOSAGE));
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MED_ID));

                    ScheduleEvent event = new ScheduleEvent();
                    event.setId(id);
                    event.setTitle(name);
                    event.setTime(time);
                    event.setDate(date);
                    event.setNote("Dosage: " + dosage);
                    event.setType("medication");
                    event.setColorRes(R.drawable.circle_purple); // Couleur pour les médicaments

                    medicationEvents.add(event);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return medicationEvents;
    }

    // Méthode pour récupérer les rendez-vous pour une date spécifique
    private List<ScheduleEvent> getAppointmentsForDate(String date, long userId) {
        List<ScheduleEvent> appointmentEvents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COLUMN_APPOINTMENT_USER_ID + " = ?" +
                " AND " + COLUMN_APPOINTMENT_DATE + " = ?" +
                " ORDER BY " + COLUMN_APPOINTMENT_TITLE + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), date});

        if (cursor.moveToFirst()) {
            do {
                try {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_TITLE));
                    String details = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_DETAILS));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_LOCATION));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_CATEGORY));
                    String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_NOTES));
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_ID));

                    // Pour les rendez-vous, nous utilisons un temps par défaut si non spécifié
                    String time = "9:00"; // Temps par défaut

                    ScheduleEvent event = new ScheduleEvent();
                    event.setId(id);
                    event.setTitle(title);
                    event.setTime(time);
                    event.setDate(date);
                    event.setNote(details + (location.isEmpty() ? "" : " at " + location));
                    event.setType("appointment");

                    // Définir la couleur en fonction de la catégorie
                    if ("doctor".equalsIgnoreCase(category)) {
                        event.setColorRes(R.drawable.circle_blue);
                    } else if ("analysis".equalsIgnoreCase(category)) {
                        event.setColorRes(R.drawable.circle_green);
                    } else {
                        event.setColorRes(R.drawable.circle_blue);
                    }

                    appointmentEvents.add(event);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return appointmentEvents;
    }

    // Méthode pour vérifier si une date a des événements
    public boolean hasEventsForDate(String date, long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean hasEvents = false;

        // Vérifier les rendez-vous
        String appointmentQuery = "SELECT COUNT(*) FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COLUMN_APPOINTMENT_USER_ID + " = ?" +
                " AND " + COLUMN_APPOINTMENT_DATE + " = ?";

        Cursor cursor = db.rawQuery(appointmentQuery, new String[]{String.valueOf(userId), date});

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                hasEvents = true;
            }
        }
        cursor.close();

        // Si nous n'avons pas trouvé d'événements, nous supposons que les médicaments sont pris tous les jours
        if (!hasEvents) {
            String medicationQuery = "SELECT COUNT(*) FROM " + TABLE_MEDICATIONS +
                    " WHERE " + COLUMN_MED_USER_ID + " = ?";

            cursor = db.rawQuery(medicationQuery, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    hasEvents = true;
                }
            }
            cursor.close();
        }

        db.close();
        return hasEvents;
    }

    // Méthode pour récupérer les dates avec des événements pour un mois spécifique
    public List<String> getDatesWithEventsForMonth(int year, int month, long userId) {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Format pour la comparaison de dates (YYYY-MM)
        String monthStr = String.format(Locale.getDefault(), "%04d-%02d", year, month);

        // Requête pour les rendez-vous
        String query = "SELECT DISTINCT " + COLUMN_APPOINTMENT_DATE +
                " FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COLUMN_APPOINTMENT_USER_ID + " = ?" +
                " AND " + COLUMN_APPOINTMENT_DATE + " LIKE '" + monthStr + "%'";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                if (!dates.contains(date)) {
                    dates.add(date);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Pour les médicaments, nous supposons qu'ils sont pris tous les jours
        // Donc si nous avons des médicaments, nous ajoutons tous les jours du mois
        String medicationQuery = "SELECT COUNT(*) FROM " + TABLE_MEDICATIONS +
                " WHERE " + COLUMN_MED_USER_ID + " = ?";

        cursor = db.rawQuery(medicationQuery, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
            // Il y a des médicaments, donc nous ajoutons tous les jours du mois
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, 1); // Mois commence à 0 dans Calendar
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int day = 1; day <= daysInMonth; day++) {
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
                if (!dates.contains(date)) {
                    dates.add(date);
                }
            }
        }
        cursor.close();

        db.close();
        return dates;
    }
}

