package com.wontondon.realmasyncissue;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    RealmConfiguration realmConfig;
    Realm realm;
    TextView textView;
    TextView workingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) this.findViewById(R.id.textView);
        workingView = (TextView) this.findViewById(R.id.workingView);
        realmConfig = new RealmConfiguration.Builder(this).build();
        realmConfig.shouldDeleteRealmIfMigrationNeeded();
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(realmConfig);
        createObjectInRealm();
        findCreatedInRealm();
        findEmptyInRealm();
    }

    private void findEmptyInRealm() {
        realm.where(Dog.class)
                .equalTo("id", 0L)
                .findFirstAsync()
                .asObservable()
                .cast(Dog.class)
                .filter(new Func1<RealmObject, Boolean>() {
                    @Override
                    public Boolean call(RealmObject realmObject) {
                        return realmObject.isLoaded();
                    }
                })
                .flatMap(new Func1<Dog, Observable<Dog>>() {
                    @Override
                    public Observable<Dog> call(Dog realmObject) {
                        if (realmObject.isValid())
                            return Observable.just(realmObject);
                        else
                            return Observable.empty();
                    }
                })
                .take(1)
                .map(new Func1<Dog, Dog>() {
                    @Override
                    public Dog call(Dog dog) {
                        dog.setName("mapped " + dog.getName());
                        return dog;
                    }
                })
                .switchIfEmpty(Observable.just(createDefaultDog()))
                .subscribe(new Action1<Dog>() {
                    @Override
                    public void call(Dog dog) {
                        textView.setText(dog.getName());
                    }
                });
    }

    private void findCreatedInRealm() {
        realm.where(Dog.class)
                .equalTo("id", 1L)
                .findFirstAsync()
                .asObservable()
                .cast(Dog.class)
                .filter(new Func1<RealmObject, Boolean>() {
                    @Override
                    public Boolean call(RealmObject realmObject) {
                        return realmObject.isLoaded();
                    }
                })
                .flatMap(new Func1<Dog, Observable<Dog>>() {
                    @Override
                    public Observable<Dog> call(Dog realmObject) {
                        if (realmObject.isValid())
                            return Observable.just(realmObject);
                        else
                            return Observable.empty();
                    }
                })
                .take(1)
                .map(new Func1<Dog, Dog>() {
                    @Override
                    public Dog call(Dog dog) {
                        realm.beginTransaction();
                        dog.setName("mapped " + dog.getName());
                        realm.commitTransaction();
                        return dog;
                    }
                })
                .switchIfEmpty(Observable.just(createDefaultDog()))
                .subscribe(new Action1<Dog>() {
                    @Override
                    public void call(Dog dog) {
                        workingView.setText(dog.getName());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    private void createObjectInRealm() {
        Dog dog = new Dog();
        dog.setId(1L);
        dog.setName("dog1");

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(dog);
        realm.commitTransaction();
    }

    private Dog createDefaultDog() {
        Dog dog = new Dog();
        dog.setName("default");
        dog.setId(123L);
        return dog;
    }
}
