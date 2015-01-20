#include <stdlib.h>
#include <windows.h>
#include <conio.h>
#include "com_vertexlabs_logging_natives_Win32ConsoleAPI.h"

int storedColours;

BOOL APIENTRY DllMain(HANDLE hModule, DWORD reason, LPVOID lpReserved)
{
    return TRUE;
}

JNIEXPORT void JNICALL Java_com_vertexlabs_logging_natives_Win32ConsoleAPI_cls(JNIEnv *env, jobject obj) 
{    
    HANDLE hConsole;
    unsigned long * hWrittenChars = 0;
    CONSOLE_SCREEN_BUFFER_INFO strConsoleInfo;
    COORD Home;
    static unsigned char EMPTY = 32;

    Home.X = 0;
    Home.Y = 0;
    
	hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    GetConsoleScreenBufferInfo(hConsole, &strConsoleInfo);
    
	FillConsoleOutputCharacter(
		hConsole, 
		EMPTY, 
		strConsoleInfo.dwSize.X * strConsoleInfo.dwSize.X, 
		Home, 
        hWrittenChars);

    SetConsoleCursorPosition(hConsole, Home);
}

JNIEXPORT void JNICALL Java_com_vertexlabs_logging_natives_Win32ConsoleAPI_setCursorPosition(JNIEnv *env, 
																							jobject obj, 
																							jshort x, 
																							jshort y) 
{
    HANDLE hConsole;
    COORD coordScreen;  

    hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    coordScreen.X = x;
    coordScreen.Y = y;
    SetConsoleCursorPosition( hConsole, coordScreen );
}

JNIEXPORT void JNICALL Java_com_vertexlabs_logging_natives_Win32ConsoleAPI_setColor(JNIEnv *env, 
																				   jobject obj, 
																				   jshort foreground, 
																				   jshort background) 
{
    HANDLE hConsole;

    hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    SetConsoleTextAttribute(hConsole, foreground + background);
}

JNIEXPORT void JNICALL Java_com_vertexlabs_logging_natives_Win32ConsoleAPI_keepColors(JNIEnv *env, 
																					 jobject obj) 
{
    HANDLE hConsole;
    CONSOLE_SCREEN_BUFFER_INFO ConsoleInfo;

    hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    GetConsoleScreenBufferInfo(hConsole, &ConsoleInfo);
    storedColours = ConsoleInfo.wAttributes;
}

JNIEXPORT void JNICALL Java_com_vertexlabs_logging_natives_Win32ConsoleAPI_restoreColors(JNIEnv *env, 
																						jobject obj) 
{
    HANDLE hConsole;

    hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    SetConsoleTextAttribute(hConsole, storedColours);
}

JNIEXPORT jint JNICALL Java_com_vertexlabs_logging_natives_Win32ConsoleAPI_getch(JNIEnv *env, jobject obj)
{	
    int ch = _getch();
    return ch;
}