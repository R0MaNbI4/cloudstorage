package ru.rompet.cloudstorage.client;

public class HelpMessage {
    final String helpMessage =
            """
                    Команды:
                    register <login> <password> - регистрация
                    auth <login> <password> - авторизация
                    save [-rw\\-rn\\-nr\\-cd] <from path> [to path] - сохранить файл\\директорию с клиента на сервер
                    load [-rw\\-rn\\-nr\\-cd]<from path> [to path] - загрузить файл\\директорию с сервера на клиент
                    delete [-nr] <path> - удалить файл\\директорию на сервере
                    create [-nr] <path> - создать новую директорию
                    move [-nr\\-cd] - переместить файл\\директорию на сервере
                    dir [path] - получить структуру директории

                    Параметры:
                    -rw (rewrite) если файл\\директория уже существует, то перезаписать
                    -rn (rename) если файл\\директория существует, то сохранить с другим именем
                    -nr (non-recursion) применить для файлов первой уровни вложенности, не затрагивая вложенные директории
                    -cd (create directories) создать несуществующие директории

                    Примечание:
                    Без указания пути назначения (to path), он неявно равен from path

                    При указании пути назначения (to path) необходимо указывать имя файла с расширением.
                    save directory\\name.txt otherDirectory\\ - неверно
                    save directory\\name.txt otherDirectory\\test1.txt - верно

                    Так же это позволяет переименовывать файл при сохранении на сервер
                    save directory\\name.txt directory\\otherName.txt""";

    @Override
    public String toString() {
        return helpMessage;
    }
}
