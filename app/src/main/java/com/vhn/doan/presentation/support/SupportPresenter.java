package com.vhn.doan.presentation.support;

import android.net.Uri;
import android.util.Log;

import com.vhn.doan.data.SupportTicket;
import com.vhn.doan.data.repository.SupportRepository;
import com.vhn.doan.presentation.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

/**
 * Presenter xử lý logic cho màn hình Support
 */
public class SupportPresenter extends BasePresenter<SupportContract.View> implements SupportContract.Presenter {

    private static final String TAG = "SupportPresenter";
    private final SupportRepository repository;

    @Inject
    public SupportPresenter(SupportRepository repository) {
        this.repository = repository;
    }

    @Override
    public void attachView(SupportContract.View view) {
        super.attachView(view);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    @Override
    public void start() {
        // Khởi tạo presenter - load dữ liệu ban đầu nếu cần
        loadUserTickets();
    }

    @Override
    public void loadUserTickets() {
        if (!isViewAttached()) return;

        view.showLoading();

        repository.getUserTickets(new SupportRepository.OnTicketsLoadedListener() {
            @Override
            public void onSuccess(List<SupportTicket> tickets) {
                if (!isViewAttached()) return;

                view.hideLoading();
                if (tickets.isEmpty()) {
                    view.showEmptyTickets();
                } else {
                    view.showTickets(tickets);
                }
            }

            @Override
            public void onError(String error) {
                if (!isViewAttached()) return;

                view.hideLoading();
                view.showError(error);
                Log.e(TAG, "Error loading tickets: " + error);
            }
        });
    }

    @Override
    public void createTicket(SupportTicket ticket) {
        if (!isViewAttached()) return;

        // Validate ticket
        if (ticket.getSubject() == null || ticket.getSubject().trim().isEmpty()) {
            view.showTicketSubmitError("Vui lòng nhập tiêu đề");
            return;
        }

        if (ticket.getDescription() == null || ticket.getDescription().trim().isEmpty()) {
            view.showTicketSubmitError("Vui lòng nhập mô tả chi tiết");
            return;
        }

        if (ticket.getType() == null) {
            view.showTicketSubmitError("Vui lòng chọn loại vấn đề");
            return;
        }

        view.showLoading();

        repository.createTicket(ticket, new SupportRepository.OnTicketCreatedListener() {
            @Override
            public void onSuccess(String ticketId) {
                if (!isViewAttached()) return;

                view.hideLoading();
                view.showTicketCreatedSuccess(ticketId);
                Log.d(TAG, "Ticket created: " + ticketId);
            }

            @Override
            public void onError(String error) {
                if (!isViewAttached()) return;

                view.hideLoading();
                view.showTicketSubmitError(error);
                Log.e(TAG, "Error creating ticket: " + error);
            }
        });
    }

    @Override
    public void uploadScreenshot(Uri imageUri) {
        if (!isViewAttached()) return;

        if (imageUri == null) {
            view.showScreenshotUploadError("Vui lòng chọn ảnh");
            return;
        }

        view.showLoading();

        repository.uploadScreenshot(imageUri, new SupportRepository.OnScreenshotUploadedListener() {
            @Override
            public void onSuccess(String downloadUrl) {
                if (!isViewAttached()) return;

                view.hideLoading();
                view.showScreenshotUploaded(downloadUrl);
                Log.d(TAG, "Screenshot uploaded: " + downloadUrl);
            }

            @Override
            public void onError(String error) {
                if (!isViewAttached()) return;

                view.hideLoading();
                view.showScreenshotUploadError(error);
                Log.e(TAG, "Error uploading screenshot: " + error);
            }
        });
    }

    @Override
    public void submitTicketWithScreenshot(SupportTicket ticket, Uri imageUri) {
        if (!isViewAttached()) return;

        if (imageUri != null) {
            // Upload screenshot trước, sau đó tạo ticket
            view.showLoading();

            repository.uploadScreenshot(imageUri, new SupportRepository.OnScreenshotUploadedListener() {
                @Override
                public void onSuccess(String downloadUrl) {
                    if (!isViewAttached()) return;

                    // Gắn URL screenshot vào ticket
                    ticket.setScreenshotUrl(downloadUrl);
                    createTicket(ticket);
                }

                @Override
                public void onError(String error) {
                    if (!isViewAttached()) return;

                    view.hideLoading();
                    view.showError("Không thể tải ảnh lên. Bạn có muốn tiếp tục gửi yêu cầu không?");
                    // Có thể cho phép người dùng chọn gửi mà không có ảnh
                    Log.e(TAG, "Error uploading screenshot: " + error);
                }
            });
        } else {
            // Không có screenshot, tạo ticket trực tiếp
            createTicket(ticket);
        }
    }
}

