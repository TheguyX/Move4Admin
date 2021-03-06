package com.example.jeff.move4admin.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.jeff.move4admin.Library.Like;
import com.example.jeff.move4admin.Library.Product;
import com.example.jeff.move4admin.Library.ServerLoader;
import com.example.jeff.move4admin.Library.ServerRequestHandler;
import com.example.jeff.move4admin.Library.adapters.AllCategoriesAdapter;
import com.example.jeff.move4admin.Library.adapters.ProductAdapter;
import com.example.jeff.move4admin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProductFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProductFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Menu menu;
    private MenuItem m_add;
    private MenuItem m_save;
    private MenuItem m_edit;

    private ArrayList<Like> categoriesList = new ArrayList<Like>();
    private ArrayList<Product> productList = new ArrayList<Product>();
    private Context mContext;
    private FrameLayout f_viewFrame;
    private FrameLayout f_editFrame;

    private ListView l_productListView;
    private ImageView i_productImage;
    private ImageView i_addProductImage;
    private TextView t_productNameLabel;
    private TextView t_addProductLabel;
    private TextView t_productDesc;
    private TextView t_productCategory;
    private TextView t_productCategoryLabel;
    private Spinner s_productCategory;
    private EditText e_productName;
    private EditText e_productDesc;
    private String savedPath;
    private ContextWrapper cw;

    private OnFragmentInteractionListener mListener;

    public ProductFragment() {
        // Required empty public constructor
    }

    public static ProductFragment newInstance(String param1, String param2) {
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add, menu);
        this.menu = menu;
        m_add = menu.findItem(R.id.action_add);
        m_save = menu.findItem(R.id.action_save);
        m_edit = menu.findItem(R.id.action_edit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add:
                // reset the labels we use for showing when we edit something
                t_addProductLabel.setText("");
                t_addProductLabel.setVisibility(View.VISIBLE);
                // sets the buttons
                f_viewFrame.setVisibility(View.GONE);
                f_editFrame.setVisibility(View.VISIBLE);
                m_add.setVisible(false);
                m_edit.setVisible(false);
                m_save.setVisible(true);
                e_productDesc.setText("");
                e_productName.setText("");

                // resets the layout
                s_productCategory.setSelection(0);
                i_addProductImage.setImageResource(R.drawable.no_product);
                return true;
            case R.id.action_save:
                final String name = e_productName.getText().toString();
                final String desc = e_productDesc.getText().toString();
                boolean duplicate = false;
                for (Product p : productList) {
                    if (p.getName().equals(name)) {
                        duplicate = true;
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Overwrite");
                        alert.setMessage("Product already exists, do you want to overwrite it?");
                        // Set an EditText view to get user input
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Bitmap b = ((BitmapDrawable) i_addProductImage.getDrawable()).getBitmap();
                                b = Bitmap.createScaledBitmap(b, 200, 200, false);
                                String img = base64(i_addProductImage.getDrawable());
                                int categoryid = categoriesList.get(s_productCategory.getSelectedItemPosition()).getcategoryID();
                                f_editFrame.setVisibility(View.GONE);
                                f_viewFrame.setVisibility(View.VISIBLE);
                                m_save.setVisible(false);
                                saveProduct(name, categoryid, img, desc, b);
                            }
                        });
                        alert.show();
                        break;
                    }
                }
                if (!duplicate) {
                    Bitmap b = ((BitmapDrawable) i_addProductImage.getDrawable()).getBitmap();
                    b = Bitmap.createScaledBitmap(b, 200, 200, false);
                    String img = base64(i_addProductImage.getDrawable());
                    int categoryid = categoriesList.get(s_productCategory.getSelectedItemPosition()).getcategoryID();
                    f_editFrame.setVisibility(View.GONE);
                    f_viewFrame.setVisibility(View.VISIBLE);
                    m_save.setVisible(false);
                    saveProduct(name, categoryid, img, desc, b);
                }
                return true;


            case R.id.action_edit:
                // sets the buttons
                f_viewFrame.setVisibility(View.GONE);
                f_editFrame.setVisibility(View.VISIBLE);
                m_add.setVisible(false);
                m_edit.setVisible(false);
                m_save.setVisible(true);

                // resets the layout
                t_addProductLabel.setText(t_productNameLabel.getText());
                t_addProductLabel.setVisibility(View.VISIBLE);
                e_productDesc.setText(t_productDesc.getText());
                e_productName.setText(t_productNameLabel.getText());
                int position = 0;
                for (Like c : categoriesList) {
                    if (c.getcategoryName().equals(t_productCategory.getText().toString())) {
                        s_productCategory.setSelection(position);
                        break;
                    }
                    position++;
                }
                i_addProductImage.setImageDrawable(i_productImage.getDrawable());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_product, container, false);
        cw = new ContextWrapper(getActivity());
        savedPath = cw.getDir("imageDir", Context.MODE_PRIVATE).toString();
        /////////////////////// view///////////////////////////////////////
        f_viewFrame = (FrameLayout) v.findViewById(R.id.productViewFrame);
        f_editFrame = (FrameLayout) v.findViewById(R.id.productEditFrame);
        l_productListView = (ListView) v.findViewById(R.id.productList);
        i_productImage = (ImageView) v.findViewById(R.id.i_productImage);
        t_productNameLabel = (TextView) v.findViewById(R.id.t_productLabel);
        t_productDesc = (TextView) v.findViewById(R.id.t_productInfo);
        t_productCategory = (TextView) v.findViewById(R.id.t_productCategory);
        t_productCategoryLabel = (TextView) v.findViewById(R.id.t_productCategoryLabel);
        ///////////////////////////////////////////////////////////////////

        /////////////////////// edit//////////////////////////////////////
        e_productDesc = (EditText) v.findViewById(R.id.e_addProductDesc);
        e_productName = (EditText) v.findViewById(R.id.e_addProductName);
        t_addProductLabel = (TextView) v.findViewById(R.id.t_addProductLabel);
        s_productCategory = (Spinner) v.findViewById(R.id.s_addProductCategory);
        i_addProductImage = (ImageView) v.findViewById(R.id.i_addImage);
        i_addProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });
        //////////////////////////////////////////////////////////////////
        getCategories();
        f_editFrame.setVisibility(View.GONE);
        f_viewFrame.setVisibility(View.VISIBLE);
        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onProductInteraction(uri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int RESULT_CANCELED = 0;
        int RESULT_OK = -1;
        int RESULT_FIRST_USER = 1;

        if (resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(cw.getContentResolver().openInputStream(targetUri));
                Bitmap b = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
                i_addProductImage.setImageBitmap(b);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void getProducts() {
        if (isOnline()) {
            ServerRequestHandler.getAllProducts(new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonArray) {
                    productList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject o = jsonArray.getJSONObject(i);
                            Product p = new Product();
                            int id = o.getInt("id");
                            String name = o.getString("name");
                            int cat;
                            String image;
                            String desc;

                            if (!o.isNull("categoryID")) {
                                cat = o.getInt("categoryID");
                                p.setCategoryID(cat);
                            }

                            if (!o.isNull("image")) {
                                image = o.getString("image");
                                p.setImage(image);
                            }

                            if (!o.isNull("description")) {
                                desc = o.getString("description");
                                p.setDescription(desc);
                            }

                            p.setProductID(id);
                            p.setName(name);

                            productList.add(p);

                        } catch (Exception e) {
                            Log.e("error", e.toString());
                        }
                    }
                    // set adapter after filling the list of products
                    if (l_productListView != null) {

                        l_productListView.setAdapter(new ProductAdapter(mContext, productList));
                        l_productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                // function responsible for the clicks in the product list
                                productListClick(adapterView, view, i, l);
                                f_viewFrame.setVisibility(View.VISIBLE);
                                f_editFrame.setVisibility(View.GONE);
                                m_save.setVisible(false);
                                m_add.setVisible(true);
                            }
                        });
                        l_productListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                // function responsible for the product list long clicks
                                productListLongClick(adapterView, view, i, l);
                                return true;
                            }
                        });
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("error", volleyError.toString());

                }
            }, mContext);
        }
    }

    public void productListLongClick(AdapterView<?> arg0, final View arg1,
                                     int position, long arg3) {
        if (isOnline()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Delete");
            alert.setMessage("Do you want to delete this category ?");
            // Set an EditText view to get user input
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    TextView txtview = (TextView) arg1.findViewById(R.id.rowLayoutName);
                    final String text = txtview.getText().toString();
                    ServerRequestHandler.DeleteProduct(new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                String s = jsonObject.getString("returnvalue");
                                if (s.equals("succes")) {
                                    getProducts();
                                } else {
                                    Log.e("error", s);
                                    Log.e("text = ", text);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    }, text, mContext);
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            alert.show();
        }
    }

    public void productListClick(AdapterView<?> adapterView, View view, int Position, long id) {
        Product p = productList.get(Position);
        t_productNameLabel.setText(p.getName());
        for (Like c : categoriesList) {
            if (c.getcategoryID() == p.getCategoryID()) {
                t_productCategory.setText(c.getcategoryName());
                t_productCategoryLabel.setVisibility(View.VISIBLE);
                break;
            }
        }
        m_edit.setVisible(true);
        t_productNameLabel.setVisibility(View.VISIBLE);
        t_productDesc.setText(p.getDescription());

        try {
            File f = new File(savedPath, p.getImage().substring(7));
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            b = Bitmap.createScaledBitmap(b, 800, 800, true);
            i_productImage.setImageBitmap(b);
        } catch (Exception e) {
            Bitmap noimg = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.no_product);
            noimg = Bitmap.createScaledBitmap(noimg, 800, 800, true);
            i_productImage.setImageBitmap(noimg);
        }
    }

    public void getCategories() {
        if (isOnline()) {
            final ArrayList<Like> categories = new ArrayList<Like>();
            ServerRequestHandler.getAllCategories(new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonArray) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject o = jsonArray.getJSONObject(i);

                            int id = o.getInt("id");
                            String name = o.getString("name");

                            Like l = new Like(id, name);
                            categories.add(l);
                        } catch (Exception e) {

                        }
                    }
                    AllCategoriesAdapter ac = new AllCategoriesAdapter(mContext, categories);
                    s_productCategory.setAdapter(ac);
                    categoriesList = categories;
                    getProducts();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            }, mContext);
        }
    }

    public void saveProduct(final String name, final int categoryID, final String image, final String desc, final Bitmap bitmap) {
        if (isOnline()) {
            ServerRequestHandler.uploadProduct(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        Log.e("returnvalue", jsonObject.getString("returnvalue"));
                        String filepath = jsonObject.getString("returnvalue");
                        ServerLoader.getInstance(mContext).saveToInternalSorage(bitmap, filepath);
                        getProducts();


                        for (Like c : categoriesList) {
                            if (c.getcategoryID() == categoryID) {
                                t_productCategory.setText(c.getcategoryName());
                                break;
                            }
                        }
                        t_productDesc.setText(desc);
                        t_productNameLabel.setText(name);
                        i_productImage.setImageBitmap(bitmap);
                        m_add.setVisible(true);
                        m_edit.setVisible(true);
                    } catch (JSONException e) {
                        Log.e("error", e.toString());
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                    Log.e("error", volleyError.toString());
                }
            }, name, categoryID, image, desc, mContext);
        }
    }

    public String base64(Drawable d) {
        Bitmap b = ((BitmapDrawable) d).getBitmap();
        b = Bitmap.createScaledBitmap(b, 200, 200, false);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        return Base64.encodeToString(bitmapdata, 1);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onProductInteraction(Uri uri);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean output = netInfo != null && netInfo.isConnectedOrConnecting();
        if (!output)
        {
            Toast.makeText(mContext, "Geen internet verbinding", Toast.LENGTH_SHORT).show();
        }
        return output;
    }

}

