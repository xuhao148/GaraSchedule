package org.aya.garaschedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.OutputStream;

public class DocActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc);
        getActionBar().setDisplayShowHomeEnabled(false);
        setTitle(getString(R.string.label_preview));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.pdf_output).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_TITLE,"schedule.pdf");
                startActivityForResult(intent,1145);
                return true;
            }
        });

        menu.add(R.string.dimension_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {

                    SchDBManager dbm = SchDBManager.getInstance();

                    LayoutInflater li = LayoutInflater.from(DocActivity.this);
                    View inflated = li.inflate(R.layout.pdf_layout_settings, null);

                    // Read current value in
                    EditText eMargin = inflated.findViewById(R.id.pdf_layout_margin);
                    EditText eWidth = inflated.findViewById(R.id.pdf_layout_width);
                    EditText eHeight = inflated.findViewById(R.id.pdf_layout_height);

                    eMargin.setText(Integer.toString(dbm.getPref("pdfMargin")));
                    eWidth.setText(Integer.toString(dbm.getPref("pdfWidth")));
                    eHeight.setText(Integer.toString(dbm.getPref("pdfHeight")));

                    // Set the button action
                    Button b = inflated.findViewById(R.id.pdf_layout_btn_use_default);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            eMargin.setText("30");
                            eWidth.setText("598");
                            eHeight.setText("842");
                        }
                    });


                    new AlertDialog.Builder(DocActivity.this)
                            .setTitle(R.string.dimension_settings)
                            .setView(inflated)
                            .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        int iMargin = Integer.parseInt(eMargin.getText().toString());
                                        int iWidth = Integer.parseInt(eWidth.getText().toString());
                                        int iHeight = Integer.parseInt(eHeight.getText().toString());
                                        if (iMargin < 0 || iWidth < 40 || iHeight < 40) {
                                            Toast.makeText(DocActivity.this, R.string.input_too_small, Toast.LENGTH_SHORT).show();
                                        } else if (iWidth - iMargin < 40 || iHeight - iMargin < 40) {
                                            Toast.makeText(DocActivity.this, R.string.input_too_large, Toast.LENGTH_SHORT).show();
                                        } else {
                                            dbm.setPref("pdfWidth", iWidth);
                                            dbm.setPref("pdfHeight", iHeight);
                                            dbm.setPref("pdfMargin", iMargin);
                                            Toast.makeText(DocActivity.this, R.string.pref_set, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (NumberFormatException nex) {
                                        Toast.makeText(DocActivity.this, R.string.number_format_ex, Toast.LENGTH_SHORT).show();
                                    } catch (Exception ex) {
                                        Toast.makeText(DocActivity.this, R.string.err_internal, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .show();
                    return true;
                } catch (Exception ex) {
                    Toast.makeText(DocActivity.this, ex.toString()+" "+ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        });

        return true;
    }

    private PdfDocument getPDF() {
        try {
            SchDBManager sm = SchDBManager.getInstance();
            final int width = sm.getPref("pdfWidth",598);
            final int height = sm.getPref("pdfHeight",842);
            final int margin = sm.getPref("pdfMargin",30);
            final int view_width = width - 2 * margin;
            final int view_height = height - 2 * margin;
            PdfDocument doc = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width,height,1).create();
            PdfDocument.Page page = doc.startPage(pageInfo);

            DocTableView v = new DocTableView(DocActivity.this);
            v.setRight(view_width);
            v.setBottom(view_height);

            Canvas c = page.getCanvas();
            c.translate(margin,margin);
            v.draw(c);

            doc.finishPage(page);
            // WIP
            return doc; // Do not forget to close it.
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == 1145 && resultCode == RESULT_OK) {
                Uri u = data.getData();
                PdfDocument doc = getPDF();
                File f = new File(u.getPath());
                if (doc == null) {
                    Toast.makeText(this, R.string.pdf_creation_failed, Toast.LENGTH_SHORT).show();
                    getContentResolver().delete(u, null, null);
                }
                try {
                    OutputStream os = getContentResolver().openOutputStream(u);
                    doc.writeTo(os);
                    doc.close();
                    os.flush();
                    os.close();
                    Toast.makeText(this, R.string.output_succeed, Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(this, getString(R.string.output_failed) + ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ex) {
            new AlertDialog.Builder(DocActivity.this)
                    .setTitle(ex.toString())
                    .setMessage(ex.getLocalizedMessage())
                    .setPositiveButton("OK",null)
                    .show();
        }
    }
}
