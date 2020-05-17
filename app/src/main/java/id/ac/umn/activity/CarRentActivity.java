package id.ac.umn.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import id.ac.umn.R;
import id.ac.umn.database.DatabaseHelper;
import id.ac.umn.session.SessionManager;

import java.util.Calendar;
import java.util.HashMap;

public class CarRentActivity extends AppCompatActivity {

    protected Cursor cursor;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    Spinner spinMobil, spinDomisili, spinDurasi;
    SessionManager session;
    String email;
    int id_book;
    public String sMobil, sDomisili, sTanggal, sDurasi;
    int jmlDurasi;
    int hargaDurasi;
    int hargaTotalDurasi, hargaTotal;
    private EditText etTanggal;
    private DatePickerDialog dpTanggal;
    Calendar newCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrent);

        dbHelper = new DatabaseHelper(CarRentActivity.this);
        db = dbHelper.getReadableDatabase();

        final String[] Mobil = {"Xenia", "Avanza", "Fortuner"};
        final String[] tujuan = {"Denpasar","Jakarta", "Surabaya"};
        final String[] lama = {"0", "1", "2", "3", "4", "5"};

        spinMobil = findViewById(R.id.Mobil);
        spinDomisili = findViewById(R.id.tujuan);
        spinDurasi = findViewById(R.id.lama);

        ArrayAdapter<CharSequence> adapterMobil = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, Mobil);
        adapterMobil.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinMobil.setAdapter(adapterMobil);

        ArrayAdapter<CharSequence> adapterTujuan = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, tujuan);
        adapterTujuan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinDomisili.setAdapter(adapterTujuan);

        ArrayAdapter<CharSequence> adapterLama = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, lama);
        adapterLama.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinDurasi.setAdapter(adapterLama);

        spinMobil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sMobil = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinDomisili.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sDomisili = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinDurasi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sDurasi = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btnBook = findViewById(R.id.book);

        etTanggal = findViewById(R.id.tanggal_berangkat);
        etTanggal.setInputType(InputType.TYPE_NULL);
        etTanggal.requestFocus();
        session = new SessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        email = user.get(SessionManager.KEY_EMAIL);
        setDateTimeField();

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                perhitunganHarga();
                if (sMobil != null && sDomisili != null && sTanggal != null && sDurasi != null) {
                    if ((sMobil.equalsIgnoreCase("jakarta") && sDomisili.equalsIgnoreCase("jakarta"))
                            || (sMobil.equalsIgnoreCase("denpasar") && sDomisili.equalsIgnoreCase("denpasar"))
                            || (sMobil.equalsIgnoreCase("surabaya") && sDomisili.equalsIgnoreCase("surabaya"))) {
                        Toast.makeText(CarRentActivity.this, "Asal dan Tujuan tidak boleh sama !", Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialog dialog = new AlertDialog.Builder(CarRentActivity.this)
                                .setTitle("Ingin booking mobil sekarang?")
                                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            db.execSQL("INSERT INTO TB_BOOK (asal, tujuan, tanggal, dewasa) VALUES ('" +
                                                    sMobil + "','" +
                                                    sDomisili + "','" +
                                                    sTanggal + "','" +
                                                    sDurasi + "');");
                                            cursor = db.rawQuery("SELECT id_book FROM TB_BOOK ORDER BY id_book DESC", null);
                                            cursor.moveToLast();
                                            if (cursor.getCount() > 0) {
                                                cursor.moveToPosition(0);
                                                id_book = cursor.getInt(0);
                                            }
                                            db.execSQL("INSERT INTO TB_HARGA (username, id_book, harga_dewasa, harga_total) VALUES ('" +
                                                    email + "','" +
                                                    id_book + "','" +
                                                    hargaTotalDurasi + "','" +
                                                    hargaTotal + "');");
                                            Toast.makeText(CarRentActivity.this, "Booking berhasil", Toast.LENGTH_LONG).show();
                                            finish();
                                        } catch (Exception e) {
                                            Toast.makeText(CarRentActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                .setNegativeButton("Tidak", null)
                                .create();
                        dialog.show();
                    }
                } else {
                    Toast.makeText(CarRentActivity.this, "Mohon lengkapi data pemesanan!", Toast.LENGTH_LONG).show();
                }
            }
        });

        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.atas);
        toolbar.setTitle("Form Booking");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void perhitunganHarga() {
        if (sMobil.equalsIgnoreCase("xenia") && sDomisili.equalsIgnoreCase("jakarta")) {
            hargaDurasi = 100000;
        } else if (sMobil.equalsIgnoreCase("xenia") && sDomisili.equalsIgnoreCase("surabaya")) {
            hargaDurasi = 200000;
        } else if (sMobil.equalsIgnoreCase("xenia") && sDomisili.equalsIgnoreCase("denpasar")) {
            hargaDurasi = 150000;
        } else if (sMobil.equalsIgnoreCase("fortuner") && sDomisili.equalsIgnoreCase("jakarta")) {
            hargaDurasi = 160000;
        }else if (sMobil.equalsIgnoreCase("fortuner") && sDomisili.equalsIgnoreCase("surabaya")) {
            hargaDurasi = 150000;
        }else if (sMobil.equalsIgnoreCase("fortuner") && sDomisili.equalsIgnoreCase("denpasar")) {
            hargaDurasi = 250000;
        }else if (sMobil.equalsIgnoreCase("avanza") && sDomisili.equalsIgnoreCase("denpasar")) {
            hargaDurasi = 180000;
        } else if (sMobil.equalsIgnoreCase("avanza") && sDomisili.equalsIgnoreCase("jakarta")) {
            hargaDurasi = 100000;
        } else if (sMobil.equalsIgnoreCase("avanza") && sDomisili.equalsIgnoreCase("surabaya")) {
            hargaDurasi = 120000;
        }

        jmlDurasi = Integer.parseInt(sDurasi);

        hargaTotal = jmlDurasi * hargaDurasi;
    }

    private void setDateTimeField() {
        etTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dpTanggal.show();
            }
        });

        dpTanggal = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei",
                        "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
                sTanggal = dayOfMonth + " " + bulan[monthOfYear] + " " + year;
                etTanggal.setText(sTanggal);

            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }
}