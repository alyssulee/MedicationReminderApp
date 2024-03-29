package app.controllers;

import com.example.apiabstractions.FamilyMemberApi;
import com.google.gson.Gson;

import java.util.UUID;

import app.FamilyMemberApiImpl;
import app.JsonTransformer;
import model.FamilyMember;
import model.User;
import repository.UserRepository;
import spark.Request;

import static spark.Spark.get;
import static spark.Spark.post;

public class FamilyMemberApiController implements Controller {

    private UserRepository userRepository = new UserRepository();
    private static final String basePath = "/api/familyMember/";

    @Override
    public void registerRoutes() {

        get(basePath + "/verifyCredentials", (req, res) -> {
            createApiOrThrow(req);
            return true;
        }, new JsonTransformer());

        get(basePath + "/getDependantPatients", (req, res) -> {
            FamilyMemberApi api = createApiOrThrow(req);
            return api.getDependantPatients();
        }, new JsonTransformer());

        get(basePath + "/getPatientCredentials/:patientId", (req, res) -> {
            UUID patientId = UUID.fromString(req.params("patientId"));
            FamilyMemberApi api = createApiOrThrow(req);
            return api.getPatientCredentials(patientId);
        }, new JsonTransformer());
    }

    private FamilyMemberApi createApiOrThrow(Request request) {
        FamilyMember familyMember = getFamilyMemberOrThrow(request);
        return FamilyMemberApiImpl.createFor(familyMember);
    }

    private FamilyMember getFamilyMemberOrThrow(Request request) {
        String username = request.headers("username");
        String password = request.headers("Password");

        if (username.isEmpty() || password.isEmpty())
            throw new Error("Username or password was missing.");

        User user = userRepository.login(username, password);
        return userRepository.getFamilyMemberByID(user.getId());
    }
}
