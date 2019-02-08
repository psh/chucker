/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readystatesoftware.chuck.internal.ui.transaction;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.data.LocalCupboard;
import com.readystatesoftware.chuck.internal.ui.transaction.TransactionListFragment.OnListFragmentInteractionListener;

import java.text.DateFormat;

class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final Context context;
    private final OnListFragmentInteractionListener listener;
    private final CursorAdapter cursorAdapter;
    private final String mockBadgeText;

    private final int colorDefault;
    private final int colorRequested;
    private final int colorError;
    private final int color500;
    private final int color400;
    private final int color300;
    private final int white;
    private final int primary;
    private final int grayText;

    TransactionAdapter(Context context, OnListFragmentInteractionListener listener) {
        this.listener = listener;
        this.context = context;
        colorDefault = ContextCompat.getColor(context, R.color.chucker_status_default);
        colorRequested = ContextCompat.getColor(context, R.color.chucker_status_requested);
        colorError = ContextCompat.getColor(context, R.color.chucker_status_error);
        color500 = ContextCompat.getColor(context, R.color.chucker_status_500);
        color400 = ContextCompat.getColor(context, R.color.chucker_status_400);
        color300 = ContextCompat.getColor(context, R.color.chucker_status_300);
        white = ContextCompat.getColor(context, android.R.color.white);
        primary = ContextCompat.getColor(context, R.color.chucker_primary_color);
        grayText = ContextCompat.getColor(context, R.color.chucker_gray_text_color);
        mockBadgeText = context.getString(R.string.mock_badge_text);

        cursorAdapter = new CursorAdapter(TransactionAdapter.this.context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chucker_list_item_transaction, parent, false);
                ViewHolder holder = new ViewHolder(itemView);
                itemView.setTag(holder);
                return itemView;
            }

            @Override
            public void bindView(View view, final Context context, Cursor cursor) {
                final HttpTransaction transaction = LocalCupboard.getInstance().withCursor(cursor).get(HttpTransaction.class);
                final ViewHolder holder = (ViewHolder) view.getTag();
                holder.path.setText(transaction.getMethod() + " " + transaction.getPath());
                holder.host.setText(transaction.getHost());
                holder.start.setText(DateFormat.getTimeInstance().format(transaction.getRequestDate()));
                holder.ssl.setVisibility(transaction.isSsl() ? View.VISIBLE : View.GONE);
                if (transaction.getStatus() == HttpTransaction.Status.Complete) {
                    holder.code.setText(String.valueOf(transaction.getResponseCode()));
                    holder.size.setText(transaction.getTotalSizeString());
                    String responseMessage = transaction.getResponseMessage();
                    if (responseMessage != null && responseMessage.contains("MOCK")) {
                        holder.duration.setText(mockBadgeText);
                        holder.duration.setTextColor(white);
                        holder.duration.setBackgroundColor(primary);
                    } else {
                        holder.duration.setText(transaction.getDurationString());
                        holder.duration.setTextColor(grayText);
                        holder.duration.setBackground(null);
                    }
                } else {
                    holder.code.setText("");
                    holder.duration.setText("");
                    holder.size.setText("");
                }
                if (transaction.getStatus() == HttpTransaction.Status.Failed) {
                    holder.code.setText("!!!");
                }
                setStatusColor(holder, transaction);
                holder.transaction = transaction;
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != TransactionAdapter.this.listener) {
                            TransactionAdapter.this.listener.onListFragmentInteraction(holder.transaction);
                        }
                    }
                });
            }

            private void setStatusColor(ViewHolder holder, HttpTransaction transaction) {
                int color;
                if (transaction.getStatus() == HttpTransaction.Status.Failed) {
                    color = colorError;
                } else if (transaction.getStatus() == HttpTransaction.Status.Requested) {
                    color = colorRequested;
                } else if (transaction.getResponseCode() >= 500) {
                    color = color500;
                } else if (transaction.getResponseCode() >= 400) {
                    color = color400;
                } else if (transaction.getResponseCode() >= 300) {
                    color = color300;
                } else {
                    color = colorDefault;
                }
                holder.code.setTextColor(color);
                holder.path.setTextColor(color);
            }
        };
    }

    @Override
    public int getItemCount() {
        return cursorAdapter.getCount();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        cursorAdapter.getCursor().moveToPosition(position);
        cursorAdapter.bindView(holder.itemView, context, cursorAdapter.getCursor());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = cursorAdapter.newView(context, cursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    void swapCursor(Cursor newCursor) {
        cursorAdapter.swapCursor(newCursor);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView code;
        public final TextView path;
        public final TextView host;
        public final TextView start;
        public final TextView duration;
        public final TextView size;
        public final ImageView ssl;
        HttpTransaction transaction;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            code = view.findViewById(R.id.chucker_code);
            path = view.findViewById(R.id.chucker_path);
            host = view.findViewById(R.id.chucker_host);
            start = view.findViewById(R.id.chucker_time_start);
            duration = view.findViewById(R.id.chucker_duration);
            size = view.findViewById(R.id.chucker_size);
            ssl = view.findViewById(R.id.chucker_ssl);
        }
    }
}
