
/*
 * Copyright (c) 2018. Created by : Mahmoud Galal
 * Support: mahmoudgalal57@yahoo.com
 */

package com.mg.zeearchiver;

import android.util.Log;

import com.mg.zeearchiver.data.ArchiveItemsList;

import java.util.ArrayList;
import java.util.List;

public class Archive {
    static {
        System.loadLibrary("Rar");
        System.loadLibrary("7z");
        System.loadLibrary("zeearchiver");
        init();
    }

    /**
     * Prints all information(Formats ,codecs...etc) about the 7z library
     */
    public native void print7zInfo();

    /**
     * Must be called before any other API
     */
    static native void init();

    /**
     * Returns System Ram Size in bytes
     *
     * @return
     */
    static native long getRamSize();


    /**
     * Loads all the supported codecs and formats and fill local lists :supportedFormats,supportedCodecs
     */
    public native void loadAllCodecsAndFormats();

    /**
     * Lists all (in console stdout) contents of an archive without extraction to disk (not used)
     *
     * @param archPath       archive name including path
     * @param outputFileName full path of the fle that is used as Stdout
     * @return error code or 0
     */
    public native int listArchive(String archPath, String outputFileName);

    /**
     * opens th archive in listing mode and returns a list of items in {@link com.mg.zeearchiver.data.ArchiveItemsList }
     *
     * @param archPath  archive name including path
     * @param itemsList passed {@link com.mg.zeearchiver.data.ArchiveItemsList } object. After successful return this should
     *                  contain a flatten list of all archive items
     * @return error code or 0
     */
    public native int listArchive2(String archPath, ArchiveItemsList itemsList);

    /**
     * Extracts the supplied archive into the given directory
     *
     * @param archPath
     * @param extractionPath
     * @param callback
     * @return 0 on success ,otherwise error state
     */
    public native int extractArchive(String archPath, String extractionPath, ExtractCallback callback);

    /**
     * Start creating a new archive by compressing the supplied files
     *
     * @param archFullName          archive name including path
     * @param itemsPaths            paths of the files to compress
     * @param length                number of files in the archive
     * @param level                 @see {@link com.mg.zeearchiver.utils.Constants }
     * @param dictionary            @see {@link com.mg.zeearchiver.utils.Constants }
     * @param wordSize              @see {@link com.mg.zeearchiver.utils.Constants }
     * @param orderMode             @see {@link com.mg.zeearchiver.utils.Constants }
     * @param solidDefined          @see {@link com.mg.zeearchiver.utils.Constants }
     * @param SolidBlockSize        @see {@link com.mg.zeearchiver.utils.Constants }
     * @param method                @see {@link com.mg.zeearchiver.utils.Constants }
     * @param encryptionMethod      @see {@link com.mg.zeearchiver.utils.Constants }
     * @param formatIndex           @see {@link com.mg.zeearchiver.utils.Constants }
     * @param encryptHeaders        @see {@link com.mg.zeearchiver.utils.Constants }
     * @param encryptHeadersAllowed @see {@link com.mg.zeearchiver.utils.Constants }
     * @param pass                  password to encrypt and secure the archive
     * @param multiThread           enable multi-threading
     * @param callback              a callback to be invocked on multiple events along the compression process(completion ,error ,password....)
     * @return 0 on success ,otherwise error state
     */
    public native int createArchive(String archFullName, String[] itemsPaths, int length, int level,
                                    int dictionary, int wordSize, boolean orderMode, boolean solidDefined, long SolidBlockSize,
                                    String method, String encryptionMethod, int formatIndex, boolean encryptHeaders
            , boolean encryptHeadersAllowed, String pass, boolean multiThread, UpdateCallback callback);

    private void loadAllCodecsFormats() {
        Log.d("libTest7ZConsole", "Calling loadAllCodecsFormats");
        loadAllCodecsAndFormats();
    }

    /**
     * Called from native code to fill the List of supported formats from all loaded formats by 7z
     *
     * @param libIndex
     * @param name
     * @param UpdateEnabled
     * @param KeepName
     * @param StartSignature
     * @param mainExt
     * @param exts
     */
    private void addSupportedFormat(int libIndex, String name, boolean UpdateEnabled,
                                    boolean KeepName, String StartSignature, String mainExt, String exts) {
        ArchiveFormat af = new ArchiveFormat(libIndex, name, UpdateEnabled,
                KeepName, mainExt, exts, StartSignature);
        supportedFormats.add(af);
    }

    public List<ArchiveItemsList.ArchiveItem> getArchiveItemsList(String filePath) {
        ArchiveItemsList archiveItemsList = new ArchiveItemsList();
        try {
            int result = listArchive2(filePath, archiveItemsList);
            if (result == -1992) {
                return new ArrayList<>();
            }
            return archiveItemsList.getItems();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    /**
     * Called from native code to fill the List of supported codecs from all loaded codecs by 7z
     *
     * @param clibIndex
     * @param codecId
     * @param codecEncoderIsAssigned
     * @param codecName
     */
    private void addSupportedCodec(int clibIndex, long codecId,
                                   boolean codecEncoderIsAssigned, String codecName) {
        Codec codec = new Codec(clibIndex, codecId, codecEncoderIsAssigned, codecName);
        supportedCodecs.add(codec);
    }


    public List<ArchiveFormat> getSupportedFormats() {
        if (supportedFormats.isEmpty()) {
            loadAllCodecsFormats();
        }
        return supportedFormats;
    }

    public List<Codec> getSupportedCodecs() {
        if (supportedCodecs.isEmpty()) {
            loadAllCodecsFormats();
        }
        return supportedCodecs;
    }

    public static class ArchiveFormat {
        public int libIndex;
        public String name;
        public String mainExtension;
        public String exts;
        public boolean UpdateEnabled;
        public boolean KeepName;
        public String StartSignature;

        public ArchiveFormat(int libIndex, String name, boolean UpdateEnabled, boolean KeepName
                , String mainExt, String exts, String StartSignature) {
            this.libIndex = libIndex;
            this.name = name;
            this.UpdateEnabled = UpdateEnabled;
            this.KeepName = KeepName;
            this.mainExtension = mainExt;
            this.exts = exts;
            this.StartSignature = StartSignature;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return name;
        }
    }

    public static class Codec {
        public int codecLibIndex;
        public long codecId;
        public boolean codecEncoderIsAssigned;
        public String codecName;

        Codec(int clibIndex, long codecId, boolean codecEncoderIsAssigned, String codecName) {
            this.codecLibIndex = clibIndex;
            this.codecId = codecId;
            this.codecEncoderIsAssigned = codecEncoderIsAssigned;
            this.codecName = codecName;
        }
    }

    private List<ArchiveFormat> supportedFormats = new ArrayList<>();
    private List<Codec> supportedCodecs = new ArrayList<>();

    public interface Constants {
        int S_OK = 0x00000000;
        int S_FALSE = 0x00000001;
        int E_NOTIMPL = 0x80004001;
        int E_NOINTERFACE = 0x80004002;
        int E_ABORT = 0x80004004;
        int E_FAIL = 0x80004005;
        int STG_E_INVALIDFUNCTION = 0x80030001;
        int E_OUTOFMEMORY = 0x8007000E;
        int E_INVALIDARG = 0x80070057;
        int ERROR_NO_MORE_FILES = 0x100123;
    }
}
