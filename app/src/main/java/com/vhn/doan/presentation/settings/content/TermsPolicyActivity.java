package com.vhn.doan.presentation.settings.content;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị danh sách điều khoản và chính sách
 */
public class TermsPolicyActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TermsPolicyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_policy);

        setupViews();
        loadTermsAndPolicies();
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadTermsAndPolicies() {
        List<TermsPolicyItem> items = new ArrayList<>();

        items.add(new TermsPolicyItem(
            getString(R.string.terms_of_service),
            R.drawable.ic_document,
            TermsPolicyType.TERMS_OF_SERVICE
        ));

        items.add(new TermsPolicyItem(
            getString(R.string.privacy_policy),
            R.drawable.ic_privacy,
            TermsPolicyType.PRIVACY_POLICY
        ));

        items.add(new TermsPolicyItem(
            getString(R.string.community_guidelines),
            R.drawable.ic_community,
            TermsPolicyType.COMMUNITY_GUIDELINES
        ));

        items.add(new TermsPolicyItem(
            getString(R.string.copyright_policy),
            R.drawable.ic_copyright,
            TermsPolicyType.COPYRIGHT_POLICY
        ));

        adapter = new TermsPolicyAdapter(this, items);
        recyclerView.setAdapter(adapter);
    }

    public enum TermsPolicyType {
        TERMS_OF_SERVICE,
        PRIVACY_POLICY,
        COMMUNITY_GUIDELINES,
        COPYRIGHT_POLICY
    }

    public static class TermsPolicyItem {
        private String title;
        private int iconRes;
        private TermsPolicyType type;

        public TermsPolicyItem(String title, int iconRes, TermsPolicyType type) {
            this.title = title;
            this.iconRes = iconRes;
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public int getIconRes() {
            return iconRes;
        }

        public TermsPolicyType getType() {
            return type;
        }
    }
}

