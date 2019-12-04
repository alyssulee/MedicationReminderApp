package repository;

import database.dataTables.*;
import model.*;
import repository.MedRepository;

import java.lang.reflect.Array;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.UUID;

public class UserRepository
{
    private AppUserTable userDatabase;
    private PatientTables patientDatabase;
    private ClientTable clientDatabase;
    private FamilyMemberTable familyDatabase;
    private DoctorTable doctorDatabase;
    private PharmacistTable pharmacistDatabase;
    private PrescriptionTable prescriptionDatabase;
    private DoseTable doseDatabase;
    private ViewsDataTable viewsDatabase;
    private AppointmentTable appointmentDatabase;
    private FillsTable fillsDatabase;
    private MonitorsTable monitorsDatabase;


    public UserRepository()
    {
        userDatabase = new AppUserTable();
        clientDatabase = new ClientTable();
        patientDatabase = new PatientTables();
        familyDatabase = new FamilyMemberTable();
        doctorDatabase = new DoctorTable();
        pharmacistDatabase = new PharmacistTable();

        prescriptionDatabase = new PrescriptionTable();
        doseDatabase = new DoseTable();

        RefillOrderTable refillOrderTable = new RefillOrderTable();
        monitorsDatabase = new MonitorsTable();
        appointmentDatabase = new AppointmentTable();
        viewsDatabase = new ViewsDataTable();
        fillsDatabase = new FillsTable();
        ReceivesTable receivesTable = new ReceivesTable();
    }

    public boolean login(String username, String password)
    {
        return userDatabase.login(username, password);
    }

    public boolean addUser(User user, UserType userType)
    {
        boolean returnVal = userDatabase.addUser(user, userType);

        switch (userType)
        {
            case Patient:
                returnVal = clientDatabase.addClient(user.getId());
                returnVal &= patientDatabase.addPatient(((Patient)user));
                break;
            case FamilyMember:
                returnVal = clientDatabase.addClient(user.getId());
                returnVal &= familyDatabase.addFamily((FamilyMember)user);
                break;
            //TODO finish addUser function
            case Doctor:
                returnVal = doctorDatabase.addDoctor((Doctor)user);
                break;
            case Pharmacist:
                returnVal = pharmacistDatabase.addPharmacist((Pharmacist)user);
                break;
        }

        if(returnVal == false)
        {
            removeUser(user);
        }
        return returnVal;
    }

    public boolean removeUser(User user)
    {
        return userDatabase.removeUser(user);
    }

    public ArrayList<Pharmacist> getAllPharmacists()
    {
        return pharmacistDatabase.getAllPharmacists();
    }

    public ArrayList<Doctor> getAllDoctors()
    {
        return doctorDatabase.getAllDoctors();
    }

    public ArrayList<Patient> getAllPatients()
    {
        return patientDatabase.getAllPatients();
    }

    public boolean addSymptom(UUID idNum, String symptom)
    {
        return patientDatabase.addSymptom(idNum, symptom);
    }

    public boolean addMeasurement(UUID idNum, PatientMeasurement measure)
    {
        return patientDatabase.addMeasurement(idNum, measure);
    }

    public boolean addPatientPrescription(Patient patient, Prescription prescription)
    {
        return prescriptionDatabase.addPatientPrescription(patient, prescription);
    }

    public ArrayList<Prescription> getAllPrescriptionsByPatient(Patient patient)
    {
        return prescriptionDatabase.getAllPrescriptionsByPatient(patient);
    }

    public ArrayList<Prescription> getPatientPrescriptionsByMedication(Patient patient, Medication med)
    {
        return prescriptionDatabase.getPatientPrescriptionsByMedication(patient, med);
    }

    //Note: addDose automatically increases currentStreak and updates longest streak in db
    public boolean addDose(Client confirmer, Patient patient, Prescription prescription)
    {
        return doseDatabase.addDose(confirmer, patient, prescription);
    }

    //Note: Automatically resets currentStreak back to zero
    public void increaseMissedDosesCount(Patient patient)
    {
        patientDatabase.increaseMissedDosesCount(patient);
    }

    public boolean addViewer(Patient patient, FamilyMember familyMember)
    {
        return viewsDatabase.addViewer(patient, familyMember);
    }

    public boolean addAppointment(Doctor doctor, Patient patient, Date date, Time time)
    {
        return appointmentDatabase.addAppointment(doctor, patient, date, time);
    }

    public boolean updateAppointment(Doctor doctor, Patient patient, Date newDate, Time newTime)
    {
        return appointmentDatabase.updateAppointment(doctor, patient, newDate, newTime);
    }

    public boolean addPharmacistFillsPrescription(Pharmacist pharmacist, Prescription prescription)
    {
        return fillsDatabase.addPharmacistFillsPrescription(pharmacist, prescription);
    }

    public boolean addDoctorMonitorsPatient(Doctor doctor, Patient patient, Date startDate, Date endDate)
    {
        return monitorsDatabase.addDoctorMonitorsPatient(doctor, patient, startDate, endDate);
    }

    public ArrayList<Patient> getMonitoredPatientsByDoctor(Doctor doctor)
    {
        return monitorsDatabase.getMonitoredPatientsByDoctor(doctor);
    }


    //Tests
    public static void main(String[] args)
    {
        MedRepository medRepo = new MedRepository();
        UserRepository database = new UserRepository();

        ArrayList<String> symptoms = new ArrayList<>();
        symptoms.add("Headache");
        symptoms.add("Itchy Eyes");

        ArrayList<PatientMeasurement> pmeasure = new ArrayList<>();
        pmeasure.add(new PatientMeasurement(new BloodPressure(100, 150), 40, new Date()));
        pmeasure.add(new PatientMeasurement(new BloodPressure(500, 250), 30, new Date()));

        Patient user = new Patient (UUID.fromString("f23c4b5b-d964-475b-9622-c7d497ae7e72"), "a", "b", "c", "d", symptoms, pmeasure);
        //System.out.println(database.addUser(user, UserType.Patient));

        //Prescription p = new Prescription(UUID.randomUUID(), new Medication("DB00005"), new Date(), 15, 0.5, 0.9 ,PrescriptionFrequency.BID, 15);
        //database.addPatientPrescription((Patient)user, p);
        System.out.println(database.getAllPrescriptionsByPatient((Patient) user));
        System.out.println(database.getPatientPrescriptionsByMedication((Patient) user, new Medication("DB00005")));
        database.addDose(user, user, database.getAllPrescriptionsByPatient(user).get(0));
        database.increaseMissedDosesCount(user);

        ArrayList<Doctor> doctorList = database.getAllDoctors();
        //database.addAppointment(doctorList.get(0), user, new Date(2020, 2, 3), new Time(10, 30, 0));
        database.updateAppointment(doctorList.get(0), user, new Date(2021, 3, 4), new Time(11, 30, 0));
        //database.addDoctorMonitorsPatient(doctorList.get(0), user, new Date(2019, 1, 2), new Date(2019, 6, 7));
        System.out.println(database.getMonitoredPatientsByDoctor(doctorList.get(0)));

/*        ArrayList<Patient> patientList = database.getAllPatients();
        for(Patient p : patientList)
            System.out.println(p.toString());*/

//        System.out.println(database.removeUser(user));
//        System.out.println(database.login("c", "d"));
        FamilyMember family = new FamilyMember(UUID.randomUUID(), "a", "b", "x", "x", "Fam");
        //System.out.println(database.addUser(family, UserType.FamilyMember));
        //database.addViewer(user, family);


/*        ArrayList<Medication> meds = medRepo.getAllMedications();
        for(Medication m : meds)
        {
            System.out.println(m);
        }*/

        //System.out.println(medRepo.getMedicationByName("Tylenol"));
        //System.out.println(medRepo.getMedicationByID("DB00005"));

    }
}
