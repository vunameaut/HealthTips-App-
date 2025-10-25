package com.vhn.doan.presentation.support;

import com.vhn.doan.data.SupportTicket;
import com.vhn.doan.presentation.base.BaseView;

import java.util.List;

import android.net.Uri;

/**
 * Contract cho màn hình Support
 */
public interface SupportContract {

    interface View extends BaseView {
        void showTickets(List<SupportTicket> tickets);
        void showEmptyTickets();
        void showTicketCreatedSuccess(String ticketId);
        void showTicketSubmitError(String error);
        void showScreenshotUploaded(String url);
        void showScreenshotUploadError(String error);
        void navigateToTicketDetail(String ticketId);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadUserTickets();
        void createTicket(SupportTicket ticket);
        void uploadScreenshot(Uri imageUri);
        void submitTicketWithScreenshot(SupportTicket ticket, Uri imageUri);
    }
}

