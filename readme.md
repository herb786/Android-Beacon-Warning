# Aplicación de Alertas usando balizas BLE  

Siempre es necesario tener información de nuestro entorno y sus alrededores como horarios, costos, permisos, prohibiciones, peligros, entre otros. Esta información puede ser liberada si estamos en la proximidad de dicho establecimiento, producto, cartel o señal. En muchos casos un código QR será suficiente pero hay casos dónde el mensaje depende de la proximidad especialmente en zonas de riesgo como son las fábricas, laboratorios o sitios en construcción. En ese caso la balizas BLE puede ser de gran ayuda. La aplicación más conocida de las balizas es en las tiendas por departamento para recolectar información acerca de los consumidores cómo su recorrido, las tiendas que visitan, su consumo.

«Bluetooth» es una tecnología que sigue evolucionando para mejorar la conectividad inalámbrica y transformar nuestro entorno. En cuánto a localización espacial, dispositivos operando con bluetooth emiten micoondas a frecuencias en torno a los **2.4 GHz**. Este tipo de ondas pueden viajar a través de materiales sólidos como paderes, puertas (excluyendo las metálicas), ventanas, árboles y así ser detectadas por otros dispositivos. De este modo podemos conocer la posición relativa entre los dispotivos emisores y receptores. Existe también la tecnología GPS pero su alcance se limita a espacios abiertos debido a que las ondas de radio de alrededor **1 GHz** son muy débiles para cruzar obstáculos.

La manera más sencilla de construir está tipo de arreglo es usando balizas o «beacons». Las balizas tienen un identificador único que puede usarse para determinar su ubicación en el arreglo. También podemos saber cuán lejos estamos de una baliza usando la intensidad de la señal emitida. Está señal es usualmente medida en decibeles.

El aplicativo de ejemplo se usará para mandar mensajes de alerta cuando personal sin el indumentario adecuado este muy cerca de un agente tóxico. Las balizas estarán junto al agente tóxico para emitir la señal que será detectada por el aplicativo.

![alt text](https://s3-us-west-2.amazonaws.com/py4hacaller/ble-hazard01.jpg)

Para este ejemplo usaremos una baliza que hace uso chip Nordic NRF51822, la cual usa una pila de botón CR2032 de 3V. Este es baliza bluetooth de baja energía puede durar más de un año con tal batería. Se dice que un compenente bluetooth es de bajo consumo de energía (BLE) si la corrinte está en el rango de los microamaperios. La intensidad de la señal usada desaparecerá más allá de los **60 metros**. Más información puede encontrarse en el manual de la baliza.

![alt text](https://s3-us-west-2.amazonaws.com/py4hacaller/ble-beacon.jpg)

## Aplicativo Android
En primer lugar debemos otorgar los permisos de ubicación y bluetooth a nuestro aplicativo. Bluetooth con bajo cosnumo de energía funcionará en dispositivos android 4.3 y versiones superiores.
```java
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
Para poder detectar cualquier dispositivo bluetooth, debemos usar la clase **BluetoothAdapter**. Para conseguir una instancia de BluetoothAdapter necesitamos llamar al gestor **BluetoothManager**

```java
BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
mBluetoothAdapter = bluetoothManager.getAdapter();
```
Luego de llamar al gestor podemos usar el adaptador BluetoothAdapter para detectar dispositvos usando bluetooth. Tenemos dos modos de detectar los dispositivos, la primera es usando la interfaz **LeScanCallback** y la segunda es la nueva interfaz **ScanCallback** para Lollipop y nuevas versiones. Ambas interfaces pueden usarse para detectar dispositivos en las cercanías.

Usemos primero la clase **LeScanCallback**. Luego de comenzar la detección de deispositivos, recibiremos el nombre del dispositivo, la dirección MAC, la proximidad en decibeles e información adicional del fabricante.

```java
mBluetoothAdapter.startLeScan(myLeScanCallback);
...
BluetoothAdapter.LeScanCallback myLeScanCallback = new BluetoothAdapter.LeScanCallback() {
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        // Device with name and MAC address
        // RSSI is the proximity
        // scanRecord for extra information.
    }
};
```
Para el segundo caso, recibimos una instancia de **ScanResult** con información acerca del dispositivo, la proximidad y datos extra.

```java
bluetoothLeScanner.startScan(myScanCallback);
...
ScanCallback myScanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        int rssi = result.getRssi();
        BluetoothDevice device = result.getDevice();
        ScanRecord scanRecord = result.getScanRecord();
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
    }
};
```
## Lectura de la intensidad de la señal
Necesitamos información de cuán próximo algún visitante está del peligro. Siendo así, usaremos el valor del parámatero **RSSI** (indicador de intensidad de una señal recibida o «received signal strength indicator»). <mark>Este valor se hará mas negativo cuánto más lejos estemos del peligro.</mark> Así nuestro programa tiene esta forma

```java
if (rssi > -40){
    Log.d("MyApp","Your life is in danger!");
} else if (rssi > -70) {
    Log.d("MyApp","Too close!");
} else {
    Log.d("MyApp","Please stay away!!!");
}
```

![alt text](https://s3-us-west-2.amazonaws.com/py4hacaller/ble-hazard02.jpg)

## Servicios GATT
Algunos dispositivos bluetooth trasmiten información acerca de la potencia, proximidad, pulso, velocidad, temperatura, presión, etc. <mark>Está información puede encontrarse en los servicios GATT del dispositivo.</mark> Cada servicio GATT tiene un identificador único o UUID. Los identificadores actuán como direcciones IP que podemos usar para consultar los datos que el dispositivo transmite.

```java
BluetoothGatt mBluetoothGatt = device.connectGatt(this, true, new MyBluetoothGattCallback());
private class MyBluetoothGattCallback extends BluetoothGattCallback {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mBluetoothGatt.discoverServices();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(mUUID);
        UUID mUUID = UUID.fromString("12341804-494c-4f47-4943-544543480000");
        BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(mUUID);
        mBluetoothGatt.readCharacteristic(mBluetoothGattCharacteristic);
    }

}
```
También si el dispositivo móvil lo permite, podemos retorna datos al módulo bluetooth usando la clase **BluetoothLeAdvertiser**.

```java
BluetoothLeAdvertiser advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
AdvertiseSettings settings = new AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setTimeout(0)
        .build();
AdvertiseData advertiseData = new AdvertiseData.Builder()
        .addServiceUuid(Service_UUID)
        .setIncludeDeviceName(true)
        .build();
advertiser.startAdvertising(settings, advertiseData, bleAdvertiserCallback);
```
![alt text](https://s3-us-west-2.amazonaws.com/py4hacaller/device-2018-07-21.gif)

### Referencias
1. https://developer.android.com/guide/topics/connectivity/bluetooth-le
2. https://developer.android.com/guide/topics/connectivity/bluetooth
3. https://meetingofideas.files.wordpress.com/2013/12/ibeacons-bible-1-0.pdf
4. https://github.com/herb786/tutorial-beacon-hazard-sign
5. https://blog.bluetooth.com/proximity-and-rssi
6. https://en.wikipedia.org/wiki/Received_signal_strength_indication
7. Bluetooth® Core Specification
8. BLE-Stack User’s Guide
9. Manual Técnico del Módulo NRF51822
