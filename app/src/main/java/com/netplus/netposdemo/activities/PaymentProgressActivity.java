package com.netplus.netposdemo.activities;

import com.netplus.netposdemo.R;
import com.netplus.netposdemo.fragments.BluetoothDialogFragment;
import com.netplus.netposdemo.fragments.BluetoothDialogFragmentPrinter;
import com.netplus.netposdemo.fragments.PrinterDialogFragment;
import com.netplus.netposdemo.fragments.TerminalDialogFragment;
import com.netplus.netposdemo.utils.SmartPesaTransactionType;
import com.netplus.netposdemo.utils.UIHelper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.devices.SpTerminal;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpCardTransactionException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.error.SpTransactionException;
import smartpesa.sdk.models.currency.Currency;
import smartpesa.sdk.models.merchant.TransactionType;
import smartpesa.sdk.models.transaction.Balance;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import smartpesa.sdk.scanner.PrinterScanningCallback;
import smartpesa.sdk.scanner.TerminalScanningCallback;
import smartpesa.sdk.interfaces.TransactionCallback;
import smartpesa.sdk.interfaces.TransactionData;
import smartpesa.sdk.models.loyalty.Loyalty;
import smartpesa.sdk.models.loyalty.LoyaltyTransaction;
import smartpesa.sdk.models.transaction.Card;
import smartpesa.sdk.models.transaction.Transaction;
import smartpesa.sdk.models.printing.AbstractPrintingDefinition;
import smartpesa.sdk.models.transaction.Card;
import smartpesa.sdk.models.transaction.Transaction;
import smartpesa.sdk.models.receipt.*;
import smartpesa.sdk.devices.SpPrinterDevice;
import smartpesa.sdk.error.SpPrinterException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.interfaces.PrintingCallback;
import smartpesa.sdk.models.printing.AbstractPrintingDefinition;
import smartpesa.sdk.models.receipt.GetReceiptCallback;
import smartpesa.sdk.models.transaction.SendNotificationCallback;


public class PaymentProgressActivity extends AppCompatActivity {

    public static final String KEY_AMOUNT = "amount";
    private static final String BLUETOOTH_FRAGMENT_TAG = "bluetooth";
    private SmartPesaTransactionType transactionType;
    ProgressDialog mProgressDialog;
    @BindView(R.id.amount_tv) TextView amountTv;
    @BindView(R.id.progress_tv) TextView progressTv;
    @BindView(R.id.receipt_btn) Button receiptBtn;
    double amount;
    ServiceManager mServiceManager;
    UUID transactionID;
    List<AbstractPrintingDefinition> dataToPrint;
    Transaction transaction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_progress);
        ButterKnife.bind(this);

        //initialise service manager
        mServiceManager = ServiceManager.get(PaymentProgressActivity.this);

        transactionType = SmartPesaTransactionType.fromEnumId(1);
        receiptBtn.setVisibility(View.INVISIBLE);
        amount = getIntent().getExtras().getDouble(KEY_AMOUNT);
        amountTv.setText("Amount: "+amount);
        progressTv.setText("Enabling blueetooth..");

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        //scan for bluetooth device
        mServiceManager.scanTerminal(new TerminalScanningCallback() {
            @Override
            public void onDeviceListRefresh(Collection<SpTerminal> collection) {
                displayBluetoothDevice(collection);
            }

            @Override
            public void onScanStopped() {

            }

            @Override
            public void onScanTimeout() {

            }

            @Override
            public void onEnablingBluetooth(String s) {

            }

            @Override
            public void onBluetoothPermissionDenied(String[] strings) {

            }

        }, this);
    }

    public SmartPesa.TransactionParam buildTransactionParam(SpTerminal terminal) {


            SmartPesa.TerminalTransactionParam.Builder builder = SmartPesa.TransactionParam.newBuilder(terminal)
                    .transactionType(transactionType.getEnumId())
                    .terminal(terminal)
                    .amount(new BigDecimal(Double.valueOf(amount)))
                    .from(SmartPesa.AccountType.DEFAULT)
                    .to(SmartPesa.AccountType.SAVINGS);

            SmartPesa.TransactionParam param = builder.build();

            return param;

    }

    private void performPayment(SpTerminal spTerminal) {

        //start the transaction


        SmartPesa.TransactionParam param = buildTransactionParam(spTerminal);

        mServiceManager.performTransaction(param, new TransactionCallback() {
            @Override
            public void onProgressTextUpdate(String s) {
                progressTv.setText(s);
            }

            @Override
            public void onTransactionFinished(TransactionType transactionType, boolean isSuccess, @Nullable Transaction transaction, @Nullable SmartPesa.Verification verification, @Nullable SpCardTransactionException exception) {

            }

            @Override
            public void onTransactionApproved(TransactionData transaction2) {

                progressTv.setText("Transaction success");
                receiptBtn.setVisibility(View.VISIBLE);
                receiptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        printReceipt(transaction.getTransactionResult().getTransactionId());
                    }
                });

            }

            @Override
            public void onTransactionDeclined(SpTransactionException e, TransactionData transactionData) {
                progressTv.setText("Transaction declined");
                progressTv.setText(e.getMessage());
                if (transactionData.getTransaction() != null){
                    receiptBtn.setVisibility(View.VISIBLE);
                    transaction = transactionData.getTransaction();
                    receiptBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            printReceipt(transaction.getTransactionResult().getTransactionId());
                        }
                    });
                }

            }



            @Override
            public void onError(SpException exception) {
                progressTv.setText(exception.getMessage());
            }

            @Override
            public void onDeviceConnected(SpTerminal spTerminal) {

            }

            @Override
            public void onDeviceDisconnected(SpTerminal spTerminal) {

            }

            @Override
            public void onBatteryStatus(SmartPesa.BatteryStatus batteryStatus) {

            }

            @Override
            public void onShowSelectApplicationPrompt(List<String> list) {

            }

            @Override
            public void onShowSelectTIDPrompt(List<String> tidList) {

            }

            @Override
            public void onWaitingForCard(String s, SmartPesa.CardMode cardMode) {
                progressTv.setText("Insert/swipe card");
            }

            @Override
            public void onShowInsertChipAlertPrompt() {
                progressTv.setText("Insert chip card");
            }

            @Override
            public void onReadCard(Card card) {

            }

            @Override
            public void onShowPinAlertPrompt(int tryCounter) {
                progressTv.setText("Enter PIN on pesaPOD");
            }

            @Override
            public void onPinEntered(int tryCounter) {

            }

            @Override
            public void onShowInputPrompt() {

            }

            @Override
            public void onReturnInputStatus(SmartPesa.InputStatus inputStatus, String s) {

            }

            @Override
            public void onShowConfirmAmountPrompt() {
                progressTv.setText("Confirm amount on pesaPOD");
            }

            @Override
            public void onAmountConfirmed(boolean b) {

            }


            @Override
            public void onStartPostProcessing(String providerName, Transaction transaction) {

            }

            @Override
            public void onReturnLoyaltyBalance(Loyalty loyalty) {

            }

            @Override
            public void onShowLoyaltyRedeemablePrompt(LoyaltyTransaction loyaltyTransaction) {

            }

            @Override
            public void onLoyaltyCancelled() {

            }

            @Override
            public void onLoyaltyApplied(LoyaltyTransaction loyaltyTransaction) {

            }

            @Override
            public void onShowConfirmFeePrompt(TransactionType.FeeChargeType feeChargeType, Currency currency, BigDecimal feeAmount, BigDecimal finalAmount) {

            }

            @Override
            public void onRequestForInput() {

            }

            @Override
            public void onShowBalance(Balance balance) {

            }

            @Override
            public void onShowPinPass(String pin) {

            }
        }, this);
    }

    //print receipt
    private void printReceipt(final UUID transactionId) {

        final SmartPesa.ReceiptFormat[] receiptFormats = {
                SmartPesa.ReceiptFormat.CUSTOMER,
                SmartPesa.ReceiptFormat.MERCHANT
        };
        new AlertDialog.Builder(PaymentProgressActivity.this)
                .setTitle(getString(R.string.select_receipt_format))
                .setAdapter(
                        new ArrayAdapter<SmartPesa.ReceiptFormat>(
                                PaymentProgressActivity.this,
                                android.R.layout.simple_list_item_1,
                                receiptFormats
                        ),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SmartPesa.ReceiptFormat receiptFormat = receiptFormats[which];
                                fetchReceiptAndPrint(transactionId, receiptFormat);
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();

    }


    //print receipt start here
    protected void fetchReceiptAndPrint(UUID transactionId, SmartPesa.ReceiptFormat receiptFormat) {
        HashMap<String, Object> config = new HashMap<>();
        final ProgressDialog mp = new ProgressDialog(this);
        mp.setTitle(getString(R.string.app_name));
        mp.setMessage(getString(R.string.loading_receipt));
        mp.show();
        mServiceManager.getReceipt(transactionId, receiptFormat, config, new GetReceiptCallback() {
            @Override
            public void onSuccess(List<AbstractPrintingDefinition> abstractPrintingDefinitions) {

                mp.dismiss();
                dataToPrint = abstractPrintingDefinitions;

                mServiceManager.scanPrinter(new PrinterScanningCallback() {
                    @Override
                    public void onDeviceListRefresh(Collection<SpPrinterDevice> collection) {

                        displayPrinterDevice(collection);
                    }

                    @Override
                    public void onScanStopped() {

                    }

                    @Override
                    public void onScanTimeout() {

                    }

                    @Override
                    public void onEnablingBluetooth(String s) {

                    }

                    @Override
                    public void onBluetoothPermissionDenied(String[] strings) {

                    }
                });
            }

            @Override
            public void onError(SpException exception) {


                if (exception instanceof SpSessionException) {
                    mp.dismiss();
                    //show the expired message
                    UIHelper.showToast(PaymentProgressActivity.this, getResources().getString(R.string.session_expired));
                    //finish
                    finish();

                } else {
                    mp.dismiss();
                    UIHelper.showErrorDialog(PaymentProgressActivity.this, getResources().getString(R.string.app_name), exception.getMessage());
                }
            }
        });
    }


    //display the list of bluetooth devices
    public void displayBluetoothDevice(Collection<SpTerminal> devices) {
        TerminalDialogFragment dialog;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (fragment == null) {
            dialog = new TerminalDialogFragment();
            dialog.show(getSupportFragmentManager(), BLUETOOTH_FRAGMENT_TAG);
        } else {
            dialog = (TerminalDialogFragment) fragment;
        }
        dialog.setSelectedListener(new DeviceSelectedListenerImpl());
        dialog.updateDevices(devices);
    }

    //start the transaction when the bluetooth device is selected
    private class DeviceSelectedListenerImpl implements BluetoothDialogFragment.DeviceSelectedListener<SpTerminal> {
        @Override
        public void onSelected(SpTerminal device) {
            performPayment(device);
        }
    }


    private void displayPrinterDevice(Collection<SpPrinterDevice> devices) {
        //try {
        PrinterDialogFragment dialog;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (fragment == null) {

            dialog = new PrinterDialogFragment();
            dialog.show(getSupportFragmentManager(), BLUETOOTH_FRAGMENT_TAG);
        } else {
            dialog = (PrinterDialogFragment) fragment;
        }
        dialog.setSelectedListener(new PrinterSelectedImpl());
        dialog.updateDevices(devices);
        // }
        //catch (Exception ex){

        //}
    }

    private class PrinterSelectedImpl implements BluetoothDialogFragmentPrinter.TerminalSelectedListener<SpPrinterDevice> {
        @Override
        public void onSelected(SpPrinterDevice device)
        {
            performPrint(device);
            if (mServiceManager != null) {
                mServiceManager.stopScan();
            }
        }

        @Override
        public void onCancelled() {

        }
    }

    private void performPrint(SpPrinterDevice device) {
        closeDialogFragment();
        mServiceManager.performPrint(SmartPesa.PrintingParam.withData(dataToPrint).printerDevice(device).build(), new PrintingCallback() {
            @Override
            public void onPrinterError(SpPrinterException errorMessage) {
                //if (isActivityDestroyed()) return;
                UIHelper.showErrorDialog(PaymentProgressActivity.this, getResources().getString(R.string.app_name), errorMessage.getMessage());
            }

            @Override
            public void onPrinterSuccess() {
                //if (isActivityDestroyed()) return;
                UIHelper.log("here");
                closeDialogFragment();
            }
        });
    }

    //close the printer bluetooth list if already one is present
    private void closeDialogFragment() {
        Fragment dialogBluetoothList = getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (dialogBluetoothList != null) {
            DialogFragment dialogFragment = (DialogFragment) dialogBluetoothList;
            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }
        }
    }


}
