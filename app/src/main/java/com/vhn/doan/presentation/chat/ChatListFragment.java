package com.vhn.doan.presentation.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.vhn.doan.R;
import com.vhn.doan.data.Conversation;
import com.vhn.doan.data.repository.ChatRepositoryImpl;

import java.util.List;

/**
 * Fragment hiển thị danh sách các cuộc trò chuyện chat AI
 */
public class ChatListFragment extends Fragment implements ChatListContract.View {

    private static final String TAG = "ChatListFragment";

    // Views
    private RecyclerView rvConversations;
    private MaterialButton btnNewChat;
    private MaterialButton btnStartNewChat;
    private MaterialButton btnLoadMore;
    private View layoutLoading;
    private View layoutEmpty;
    private View layoutLoadMore;
    private View layoutLoadingMore;

    // Components
    private ConversationAdapter conversationAdapter;
    private ChatListContract.Presenter presenter;

    public static ChatListFragment newInstance() {
        return new ChatListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        initPresenter();
        presenter.attachView(this);
    }

    private void initViews(View view) {
        rvConversations = view.findViewById(R.id.rv_conversations);
        btnNewChat = view.findViewById(R.id.btn_new_chat);
        btnStartNewChat = view.findViewById(R.id.btn_start_new_chat);
        btnLoadMore = view.findViewById(R.id.btn_load_more);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutLoadMore = view.findViewById(R.id.layout_load_more);
        layoutLoadingMore = view.findViewById(R.id.layout_loading_more);
    }

    private void initPresenter() {
        ChatRepositoryImpl chatRepository = new ChatRepositoryImpl();
        presenter = new ChatListPresenter(chatRepository);
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter();
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConversations.setAdapter(conversationAdapter);

        conversationAdapter.setOnConversationClickListener(conversation -> {
            if (presenter != null) {
                presenter.onConversationClicked(conversation);
            }
        });
    }

    private void setupListeners() {
        btnNewChat.setOnClickListener(v -> {
            if (presenter != null) {
                presenter.onNewChatClicked();
            }
        });

        btnStartNewChat.setOnClickListener(v -> {
            if (presenter != null) {
                presenter.onNewChatClicked();
            }
        });

        btnLoadMore.setOnClickListener(v -> {
            if (presenter != null) {
                presenter.onLoadMoreClicked();
            }
        });
    }

    // Implementation của ChatListContract.View

    @Override
    public void showConversations(List<Conversation> conversations) {
        conversationAdapter.setConversations(conversations);
    }

    @Override
    public void addMoreConversations(List<Conversation> conversations) {
        conversationAdapter.addConversations(conversations);
    }

    @Override
    public void showLoadingConversations() {
        layoutLoading.setVisibility(View.VISIBLE);
        rvConversations.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    @Override
    public void hideLoadingConversations() {
        layoutLoading.setVisibility(View.GONE);
        rvConversations.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoadingMore() {
        layoutLoadingMore.setVisibility(View.VISIBLE);
        btnLoadMore.setVisibility(View.GONE);
    }

    @Override
    public void hideLoadingMore() {
        layoutLoadingMore.setVisibility(View.GONE);
        btnLoadMore.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoadMoreButton(boolean show) {
        layoutLoadMore.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            btnLoadMore.setVisibility(View.VISIBLE);
            layoutLoadingMore.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvConversations.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyState() {
        layoutEmpty.setVisibility(View.GONE);
        rvConversations.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoadConversationsError(String error) {
        showError("Lỗi tải cuộc trò chuyện: " + error);
    }

    @Override
    public void showLoadMoreError(String error) {
        showError("Lỗi tải thêm: " + error);
    }

    @Override
    public void navigateToChatDetail(String conversationId, String conversationTitle) {
        // TODO: Chuyển đến ChatDetailFragment với conversationId
        if (getActivity() != null) {
            ChatDetailFragment chatDetailFragment = ChatDetailFragment.newInstance(conversationId, conversationTitle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, chatDetailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void navigateToNewChat() {
        // TODO: Chuyển đến NewChatFragment hoặc ChatDetailFragment với conversationId mới
        if (getActivity() != null) {
            NewChatFragment newChatFragment = NewChatFragment.newInstance();

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, newChatFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showLoading() {
        showLoadingConversations();
    }

    @Override
    public void hideLoading() {
        hideLoadingConversations();
    }

    @Override
    public void onDestroyView() {
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroyView();
    }

    /**
     * Method để refresh danh sách từ bên ngoài
     */
    public void refreshConversations() {
        if (presenter != null) {
            presenter.refreshConversations();
        }
    }
}
