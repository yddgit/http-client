package com.my.project.http.client.stub;

import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Upload API Stub
 */
@Path("api")
public class ReceiveFileStub {

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(@HeaderParam("Accept-Language") String language,
                           @FormDataParam("request") Request request,
                           @FormDataParam("file") FormDataBodyPart file) throws IOException {
        System.out.println(String.format("RECEIVE_REQ ==> Accept-Language=%s", language));
        System.out.println(String.format("RECEIVE_REQ ==> request: %s", request.toString()));
        System.out.println(String.format("RECEIVE_REQ ==> file: %s, Content-Type=%s", file.getContentDisposition(), file.getMediaType()));
        if (file == null) {
            return Response.fail("upload file is empty", "uploadError", 999999);
        } else {
            BodyPartEntity bodyPartEntity = (BodyPartEntity) (file.getEntity());
            File savedFile = saveFile(bodyPartEntity, file.getContentDisposition().getFileName());
            System.out.println(String.format("RECEIVE_REQ ==> file size: %s, file path: %s", savedFile.length(), savedFile.getAbsolutePath()));
            savedFile.deleteOnExit();
            return Response.success("upload success", "uploading", 123456);
        }
    }

    private File saveFile(BodyPartEntity bodyPartEntity, String fileName) throws IOException {
        java.nio.file.Path path = FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        Files.createDirectories(path);
        java.nio.file.Path file = path.resolve(fileName);
        Files.copy(bodyPartEntity.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
        return file.toFile();
    }

    public static class Request {
        @JsonbProperty("uid")
        private String uid;
        @JsonbProperty("email")
        private String email;
        @JsonbProperty("token")
        private String token;
        @JsonbProperty("uploadType")
        private String uploadType;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUploadType() {
            return uploadType;
        }

        public void setUploadType(String uploadType) {
            this.uploadType = uploadType;
        }

        @Override
        public String toString() {
            return "Request{" +
                    "uid='" + uid + '\'' +
                    ", email='" + email + '\'' +
                    ", token='" + token + '\'' +
                    ", uploadType='" + uploadType + '\'' +
                    '}';
        }
    }

    public static class Response {
        private String errorMessage;
        private String status;

        private Integer uploadSequenceId;

        public static Response success(String message, String status, int uploadSequenceId) {
            Response response = new Response();
            response.setErrorMessage(message);
            response.setStatus(status);
            response.setUploadSequenceId(uploadSequenceId);
            return response;
        }

        public static Response fail(String message, String status, int uploadSequenceId) {
            Response response = new Response();
            response.setErrorMessage(message);
            response.setStatus(status);
            response.setUploadSequenceId(uploadSequenceId);
            return response;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getUploadSequenceId() {
            return uploadSequenceId;
        }

        public void setUploadSequenceId(Integer uploadSequenceId) {
            this.uploadSequenceId = uploadSequenceId;
        }

    }

}
