package com.anhnvt_ph55017.md_02_datn.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anhnvt_ph55017.md_02_datn.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetCardPayment extends BottomSheetDialogFragment {

    private EditText etCardHolderName;
    private EditText etCardNumber;
    private EditText etCardExpiry;
    private EditText etCardCVV;
    private boolean isFormattingCard = false;

    private CardPaymentListener listener;
    private String initialCardHolderName;
    private String initialCardNumber;
    private String initialCardExpiry;
    private String initialCardCVV;
    private boolean confirmed = false;

    public static BottomSheetCardPayment newInstance(
            String cardHolderName,
            String cardNumber,
            String cardExpiry,
            String cardCVV,
            CardPaymentListener listener
    ) {
        BottomSheetCardPayment fragment = new BottomSheetCardPayment();
        fragment.listener = listener;
        fragment.initialCardHolderName = cardHolderName;
        fragment.initialCardNumber = cardNumber;
        fragment.initialCardExpiry = cardExpiry;
        fragment.initialCardCVV = cardCVV;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_card_payment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCardHolderName = view.findViewById(R.id.etCardHolderName);
        etCardNumber = view.findViewById(R.id.etCardNumber);
        etCardExpiry = view.findViewById(R.id.etCardExpiry);
        etCardCVV = view.findViewById(R.id.etCardCVV);

        if (initialCardHolderName != null) etCardHolderName.setText(initialCardHolderName);
        if (initialCardNumber != null) etCardNumber.setText(initialCardNumber);
        if (initialCardExpiry != null) etCardExpiry.setText(initialCardExpiry);
        if (initialCardCVV != null) etCardCVV.setText(initialCardCVV);

        setupCardNumberFormatter();
        setupExpiryFormatter();

        view.findViewById(R.id.btnConfirmCard).setOnClickListener(v -> onConfirm());
        view.findViewById(R.id.btnCancelCard).setOnClickListener(v -> onCancel());
    }

    private void onConfirm() {
        String holder = etCardHolderName.getText().toString().trim();
        String number = etCardNumber.getText().toString().replaceAll("\\s+", "");
        String expiry = etCardExpiry.getText().toString().trim();
        String cvv = etCardCVV.getText().toString().trim();

        if (holder.isEmpty()) {
            Toast.makeText(requireContext(), "Nhập tên chủ thẻ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!number.matches("\\d{16}")) {
            Toast.makeText(requireContext(), "Số thẻ phải đủ 16 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            Toast.makeText(requireContext(), "Hết hạn phải theo định dạng MM/YY", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!cvv.matches("\\d{3}")) {
            Toast.makeText(requireContext(), "CVV phải là 3 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }

        confirmed = true;
        if (listener != null) {
            listener.onCardPaymentConfirmed(holder, number, expiry, cvv);
        }
        dismiss();
    }

    private void onCancel() {
        confirmed = false;
        dismiss();
    }

    private void setupCardNumberFormatter() {
        etCardNumber.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isFormattingCard) return;
                isFormattingCard = true;

                String digitsOnly = s.toString().replaceAll("\\D", "");
                if (digitsOnly.length() > 16) {
                    digitsOnly = digitsOnly.substring(0, 16);
                }

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digitsOnly.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(' ');
                    }
                    formatted.append(digitsOnly.charAt(i));
                }

                etCardNumber.setText(formatted.toString());
                etCardNumber.setSelection(formatted.length());
                isFormattingCard = false;
            }
        });
    }

    private void setupExpiryFormatter() {
        etCardExpiry.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isFormattingExpiry = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isFormattingExpiry) return;
                isFormattingExpiry = true;

                String digitsOnly = s.toString().replaceAll("\\D", "");
                if (digitsOnly.length() > 4) {
                    digitsOnly = digitsOnly.substring(0, 4);
                }

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digitsOnly.length(); i++) {
                    if (i == 2) {
                        formatted.append('/');
                    }
                    formatted.append(digitsOnly.charAt(i));
                }

                etCardExpiry.setText(formatted.toString());
                etCardExpiry.setSelection(formatted.length());
                isFormattingExpiry = false;
            }
        });
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!confirmed && listener != null) {
            listener.onCardPaymentCancelled();
        }
    }

    public interface CardPaymentListener {
        void onCardPaymentConfirmed(String cardHolderName, String cardNumber, String cardExpiry, String cardCVV);
        void onCardPaymentCancelled();
    }
}
