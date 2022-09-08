package mx.slam.myzetrack;

import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.DeviceViewHolder> {

    private final List<BLEDevice> devices;
    private static onItemClickListener listener;

    public RecyclerAdapter(List<BLEDevice> nList, onItemClickListener listener1) {
        this.devices = nList;
        listener = listener1;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_ble_device,parent,false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bind(devices.get(position));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView cardView;
        TextView deviceName, tv_uuid, tv_type;
        ImageView devicePic;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.my_cardview);
            deviceName = (TextView) itemView.findViewById(R.id.device_name);
            devicePic = (ImageView) itemView.findViewById(R.id.device_pic);
            tv_type = (TextView)itemView.findViewById(R.id.tv_type);
            tv_uuid = (TextView) itemView.findViewById(R.id.tv_uuid);
            itemView.setOnClickListener(this);
        }

        public void bind(BLEDevice device){
            String name = device.getName();
            if(name!=null) {
                name += " ("+device.getAddress()+")";
                deviceName.setText(name);
                if(name.contains("ZeTrack")){
                    devicePic.setImageResource(R.mipmap.ic_my_zetrack);
                }else{
                    devicePic.setImageResource(R.mipmap.ic_my_kronos);
                }
            }else{
                name =   R.string.unknown_dev + "("+device.getAddress()+")";
                deviceName.setText(name);
                devicePic.setImageResource(R.mipmap.ic_my_kronos);
            }
            String mType = "Type: "+device.getType();
            tv_type.setText(mType);
            StringBuilder uuids = new StringBuilder();
            uuids.append("UUID: ");
            ParcelUuid[] parcel = device.getUuids();
            if(parcel!=null) {
                for (ParcelUuid pu : parcel)
                    uuids.append(pu.getUuid().toString());
                tv_uuid.setText(uuids);
            }
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, this.getBindingAdapterPosition());
        }
    }

}
