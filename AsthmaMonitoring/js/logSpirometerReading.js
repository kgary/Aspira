﻿function createSpirometerLog() { 
    var content;
    
        Windows.Storage.KnownFolders.documentsLibrary.getFileAsync("SpirometerReadingLog.txt").then(function (datafile) {
          
            content = Windows.Storage.ApplicationData.current.localSettings.values["readingStartTime"];
            content += "\nPEF Value : ";
            content += Windows.Storage.ApplicationData.current.localSettings.values["PEFValCaptured"];
            content += "\nFEV Value : ";
            content += Windows.Storage.ApplicationData.current.localSettings.values["FEVValCaptured"];
            content += "\n\n";

            Windows.Storage.FileIO.appendTextAsync(datafile, content);
        });
    
       
}