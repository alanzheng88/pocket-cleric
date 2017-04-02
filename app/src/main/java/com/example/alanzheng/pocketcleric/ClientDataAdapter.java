package com.example.alanzheng.pocketcleric;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by alanzheng on 2017-03-31.
 */

public class ClientDataAdapter extends RecyclerView.Adapter<ClientDataAdapter.ViewHolder> {

    private Context mContext;
    private List<ClientScanResult> mClientScanResults;
    private static TextView sIpAddressTextView;
    private static TextView sHwAddressTextView;
    private static TextView sDeviceTextView;
    private static TextView sIsReachableTextView;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
            sIpAddressTextView = (TextView) view.findViewById(R.id.textview_client_ipaddress);
            sHwAddressTextView = (TextView) view.findViewById(R.id.textview_client_hwaddress);
            sDeviceTextView = (TextView) view.findViewById(R.id.textview_client_device);
            sIsReachableTextView = (TextView) view.findViewById(R.id.textview_client_isreachable);
        }

    }

    public ClientDataAdapter(List<ClientScanResult> clientScanResults, Context context) {
        mContext = context;
        mClientScanResults = clientScanResults;
        Toast.makeText(mContext, "ClientAdapter Count", Toast.LENGTH_SHORT).show();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.row_client_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ClientScanResult clientScanResult = mClientScanResults.get(position);
        sIpAddressTextView.setText(clientScanResult.getIpAddress());
        sHwAddressTextView.setText(clientScanResult.getHwAddress());
        sDeviceTextView.setText(clientScanResult.getDevice());
        String isReachable = clientScanResult.isReachable() ? "true" : "false";
        sIsReachableTextView.setText(isReachable);
    }

    @Override
    public int getItemCount() {
        if (mClientScanResults == null) {
            return 0;
        }
        return mClientScanResults.size();
    }

}
