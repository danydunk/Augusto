/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.jar.Attributes.Name;
import java.util.zip.*;
import static java.util.Arrays.*;

import org.junit.*;
import static org.junit.Assert.*;

public final class ClassLoadingAndJREMocksTest
{
   static class Foo1 {}

   @Test
   public void recordExpectationForFile(@Mocked File file)
   {
      new Expectations() {{ new File("filePath").exists(); result = true; }};

      new Foo1(); // causes a class load
      assertTrue(new File("filePath").exists());
   }

   static class Foo2 {}

   @Test
   public void mockUpFile()
   {
      new MockUp<File>() {
         @Mock void $init(String name) {} // not necessary, except to verify non-occurrence of NPE
         @Mock boolean exists() { return true; }
      };

      new Foo2(); // causes a class load
      assertTrue(new File("filePath").exists());
   }

   @Test
   public void mockFileSafelyUsingReentrantMockMethod()
   {
      new MockUp<File>() {
         @Mock
         boolean exists(Invocation inv)
         {
            File it = inv.getInvokedInstance();
            return "testFile".equals(it.getName()) || inv.<Boolean>proceed();
         }
      };

      checkForTheExistenceOfSeveralFiles();
   }

   void checkForTheExistenceOfSeveralFiles()
   {
      assertFalse(new File("someOtherFile").exists());
      assertTrue(new File("testFile").exists());
      assertFalse(new File("yet/another/file").exists());
      assertTrue(new File("testFile").exists());
   }

   @Test
   public void mockFileSafelyUsingProceed()
   {
      new MockUp<File>() {
         @Mock boolean exists(Invocation inv)
         {
            File it = inv.getInvokedInstance();
            return "testFile".equals(it.getName()) || inv.<Boolean>proceed();
         }
      };

      checkForTheExistenceOfSeveralFiles();
   }

   @Test
   public void mockFileSafelyUsingDynamicPartialMocking()
   {
      final File aFile = new File("");

      new Expectations(File.class) {{
         aFile.exists();
         result = new Delegate() {
            @Mock boolean exists(Invocation inv)
            {
               File it = inv.getInvokedInstance();
               return "testFile".equals(it.getName());
            }
         };
      }};

      checkForTheExistenceOfSeveralFiles();
   }

   @Test
   public void mockFileSafelyUsingInstantiationRecordingToMatchDesiredFuturesInstancesOnly(@Mocked File anyFile)
   {
      new Expectations() {{
         File testFile = new File("testFile");
         testFile.exists(); result = true;
      }};

      checkForTheExistenceOfSeveralFiles();
   }

   @Test
   public void mockFileOutputStreamInstantiation() throws Exception
   {
      final String fileName = "test.txt";

      new Expectations(FileOutputStream.class) {{
         OutputStream os = new FileOutputStream(fileName);
         //noinspection ConstantConditions
         os.write((byte[]) any);
         os.close();
      }};

      FileOutputStream output = new FileOutputStream(fileName);
      output.write(new byte[] {123});
      output.close();

      assertFalse(new File(fileName).exists());
   }

   @Test
   public void mockEntireAbstractListClass(@Mocked AbstractList<?> c)
   {
      assertNull(c.get(1));
   }

   @Test
   public void attemptToMockNonMockableJREClass(@Mocked Integer mock)
   {
      assertNull(mock);
   }

   static class ClassWithVector
   {
      @SuppressWarnings("UseOfObsoleteCollectionType")
      final Collection<?> theVector = new Vector<Object>();
      public int getVectorSize() { return theVector.size(); }
   }

   @Test
   public void useMockedVectorDuringClassLoading(@Mocked final Vector<?> mockedVector)
   {
      new Expectations() {{
         mockedVector.size(); result = 2;
      }};

      assertEquals(2, new ClassWithVector().getVectorSize());
   }

   @Test
   public void mockHashtable(@Mocked final Properties mock)
   {
      Properties props = new Properties();

      new Expectations() {{
         mock.remove(anyString); result = 123;
         mock.getProperty("test"); result = "mock";
      }};

      assertEquals(123, props.remove(""));
      assertEquals("mock", props.getProperty("test"));
   }

   @Test
   public void mockURLAndURLConnection(@Mocked URL mockUrl, @Mocked URLConnection mockConnection) throws Exception
   {
      URLConnection conn = mockUrl.openConnection();

      assertSame(mockConnection, conn);
   }

   @Test
   public void mockURLAndHttpURLConnection(@Mocked URL mockUrl, @Mocked HttpURLConnection mockConnection)
      throws Exception
   {
      HttpURLConnection conn = (HttpURLConnection) mockUrl.openConnection();
      assertSame(mockConnection, conn);
   }

   @Test
   public void mockURLAndHttpURLConnectionWithDynamicMock(@Mocked final HttpURLConnection mockHttpConnection)
      throws Exception
   {
      final URL url = new URL("http://nowhere");

      new Expectations(url) {{
         url.openConnection(); result = mockHttpConnection;
         mockHttpConnection.getOutputStream(); result = new ByteArrayOutputStream();
      }};

      // Code under test:
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("PUT");
      OutputStream out = conn.getOutputStream();

      assertNotNull(out);

      new Verifications() {{
         mockHttpConnection.setDoOutput(true);
         mockHttpConnection.setRequestMethod("PUT");
      }};
   }

   String readResourceContents(String hostName, int port, String resourceId, int connectionTimeoutMillis)
      throws IOException
   {
      URL url = new URL("http", hostName, port, resourceId);
      URLConnection connection = url.openConnection();

      connection.setConnectTimeout(connectionTimeoutMillis);
      connection.connect();

      Object content = connection.getContent();
      return content.toString();
   }

   @Test
   public void cascadingMockedURLWithInjectableCascadedURLConnection(
      @Mocked URL anyUrl, @Injectable final URLConnection cascadedUrlConnection)
      throws Exception
   {
      final String testContents = "testing";
      new Expectations() {{ cascadedUrlConnection.getContent(); result = testContents; }};

      String contents = readResourceContents("remoteHost", 80, "aResource", 1000);

      assertEquals(testContents, contents);
   }

   @Test
   public void mockFileInputStream() throws Exception
   {
      new Expectations(FileInputStream.class) {{ new FileInputStream("").close(); result = new IOException(); }};

      try {
         new FileInputStream("").close();
         fail();
      }
      catch (IOException ignore) {
         // OK
      }
   }

   @Test
   public void mockNonExistentZipFileSoItAppearsToExistWithTheContentsOfAnExistingFile() throws Exception
   {
      String existentZipFileName = getClass().getResource("test.zip").getPath();
      final ZipFile testZip = new ZipFile(existentZipFileName);

      new Expectations(ZipFile.class) {{
         new ZipFile("non-existent"); result = testZip;
      }};

      assertEquals("test", readFromZipFile("non-existent"));

      try {
         new ZipFile("another-non-existent");
         fail();
      }
      catch (IOException ignore) {}

      assertEquals("test", readFromZipFile(existentZipFileName));
   }

   private String readFromZipFile(String fileName) throws IOException
   {
      ZipFile zf = new ZipFile(fileName);
      ZipEntry firstEntry = zf.entries().nextElement();
      InputStream content = zf.getInputStream(firstEntry);
      return new BufferedReader(new InputStreamReader(content)).readLine();
   }

   @Test
   public void mockJarEntry(@Mocked final JarEntry mockEntry)
   {
      new Expectations() {{
         mockEntry.getName(); result = "Test";
      }};

      assertEquals("Test", mockEntry.getName());
   }

   String readMainClassAndFileNamesFromJar(File file, List<String> containedFileNames) throws IOException
   {
      JarFile jarFile = new JarFile(file);

      Manifest manifest = jarFile.getManifest();
      Attributes mainAttributes = manifest.getMainAttributes();
      String mainClassName = mainAttributes.getValue(Name.MAIN_CLASS);

      Enumeration<JarEntry> jarEntries = jarFile.entries();

      while (jarEntries.hasMoreElements()) {
         JarEntry jarEntry = jarEntries.nextElement();
         containedFileNames.add(jarEntry.getName());
      }

      return mainClassName;
   }

   @Test
   public void mockJavaUtilJarClasses(
      @Mocked final JarFile mockFile, @Mocked final Manifest mockManifest, @Mocked final Attributes mockAttributes,
      @Mocked final Enumeration<JarEntry> mockEntries, @Mocked final JarEntry mockEntry) throws Exception
   {
      final File testFile = new File("test.jar");
      final String mainClassName = "test.Main";

      new StrictExpectations() {{
         new JarFile(testFile);
         mockFile.getManifest(); result = mockManifest;
         mockManifest.getMainAttributes(); result = mockAttributes;
         mockAttributes.getValue(Name.MAIN_CLASS); result = mainClassName;
         mockFile.entries(); result = mockEntries;

         mockEntries.hasMoreElements(); result = true;
         mockEntries.nextElement(); result = mockEntry;
         mockEntry.getName(); result = "test/Main$Inner.class";

         mockEntries.hasMoreElements(); result = true;
         mockEntries.nextElement(); result = mockEntry;
         mockEntry.getName(); result = "test/Main.class";

         mockEntries.hasMoreElements(); result = false;
      }};

      List<String> fileNames = new ArrayList<String>();
      String mainClassFromJar = readMainClassAndFileNamesFromJar(testFile, fileNames);

      assertEquals(mainClassName, mainClassFromJar);
      assertEquals(asList("test/Main$Inner.class", "test/Main.class"), fileNames);
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToMockJREClassThatIsNeverMockable(@Mocked Class<?> mockClass)
   {
   }

   @Test // StackOverflowError when run by itself, due to recursive class loading of initial classes
   public void mockSystemNanoTime() {
      new MockUp<System>() {
         @Mock long nanoTime() { return 123; }
      };

      assertEquals(123, System.nanoTime());
   }
}
