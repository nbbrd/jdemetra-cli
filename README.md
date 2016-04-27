# Command line interface for JDemetra+

**jdemetra-cli** is a set of command-line tools for calling [JDemetra+](https://github.com/jdemetra/jdemetra-app/) APIs (such as seasonal adjustment, outlier detection and time series I/O) without using the desktop application.

These tools follow the Unix philosophy of _"Do one thing and do it well"_: 
- perform a single function 
- be composable

They can be used for research, training or automation.

## Quickstart

jdemetra-cli runs on multiple platforms such as Windows, Mac OS X and Linux (Java SE 8 or later version is required).  

To install, download the [latest version](https://github.com/nbbrd/jdemetra-cli/releases/latest), unzip it where you like.  
If you want to call it anywhere on your system, add `/bin` to your system path.

An exhaustive list of commands is available on the [wiki](https://github.com/nbbrd/jdemetra-cli/wiki).

The following example extracts some time series from an Excel file, detects their outliers and writes a report.

`$ dem spreadsheet2ts data.xslx | dem ts2outliers -o result.xml -f`



